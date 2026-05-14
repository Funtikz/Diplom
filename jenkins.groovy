task_branch = "${TEST_BRANCH_NAME}"
def branch_cutted = task_branch.contains("origin")
        ? task_branch.split('/')[1]
        : task_branch.trim()

currentBuild.displayName = branch_cutted

def base_git_url = "https://github.com/Funtikz/Diplom.git"
def TELEGRAM_CHAT_ID = "-1003786359995"

node {
    withEnv([
            "branch=${branch_cutted}",
            "base_url=${base_git_url}"
    ]) {

        stage("Checkout Branch") {
            if (!branch_cutted.contains("master")) {
                try {
                    getProject(base_git_url, branch_cutted)
                } catch (err) {
                    echo "Failed get branch ${branch_cutted}"
                    throw err
                }
            } else {
                echo "Current branch is master"
            }
        }

        try {
            stage("Запуск тестов") {
                runTests()
            }
        } finally {
            stage("Allure") {
                generateAllure()
            }

            stage("Telegram Notification") {
                sendTelegramReport(TELEGRAM_CHAT_ID, branch_cutted)
            }
        }
    }
}

def runTests() {
    try {
        sh """
            chmod +x gradlew
            ./gradlew clean test
        """
    } finally {
        echo "Test execution finished (some tests may have failed)"
    }
}

def getProject(String repo, String branch) {
    cleanWs()

    checkout([
            $class: 'GitSCM',
            branches: [[name: branch]],
            userRemoteConfigs: [[url: repo]]
    ])
}

def generateAllure() {
    allure([
            includeProperties: true,
            jdk: '',
            properties: [],
            reportBuildPolicy: 'ALWAYS',
            results: [[path: 'build/allure-results']]
    ])
}
def sendTelegramReport(String chatId, String branchName) {

    def summaryFile = "allure-report/widgets/summary.json"

    def passed = 0
    def failed = 0
    def broken = 0
    def skipped = 0
    def total = 0

    if (fileExists(summaryFile)) {
        def jsonText = readFile(summaryFile)
        def json = new groovy.json.JsonSlurper().parseText(jsonText)

        passed = json.statistic.passed ?: 0
        failed = json.statistic.failed ?: 0
        broken = json.statistic.broken ?: 0
        skipped = json.statistic.skipped ?: 0
        total = json.statistic.total ?: 0
    }

    def status = currentBuild.currentResult ?: "SUCCESS"

    def statusEmoji = "ℹ️"
    if (status == "SUCCESS") statusEmoji = "✅"
    else if (status == "FAILURE") statusEmoji = "❌"
    else if (status == "UNSTABLE") statusEmoji = "⚠️"

    def successRate = total > 0
            ? String.format('%.1f', (passed * 100.0 / total))
            : "0.0"

    def allureUrl = "${env.BUILD_URL}allure/"

    def message = """
${statusEmoji} Jenkins Build Report

📦 Job: ${env.JOB_NAME}
🔢 Build: #${env.BUILD_NUMBER}
🌿 Branch: ${branchName}
📌 Status: ${status}

📊 Test Summary:
✅ Passed: ${passed}
❌ Failed: ${failed}
💥 Broken: ${broken}
⏭ Skipped: ${skipped}
📦 Total: ${total}
📈 Success Rate: ${successRate}%

🔗 Allure Report:
${allureUrl}
"""

    withCredentials([
            string(credentialsId: 'telegram-bot-token', variable: 'BOT_TOKEN')
    ]) {

        // 1. send text
        sh """
            curl -s -X POST "https://api.telegram.org/bot\$BOT_TOKEN/sendMessage" \
              -d "chat_id=${chatId}" \
              --data-urlencode "text=${message}"
        """

        // 2. generate SVG pie chart (NO python, NO libs)
        sh """
cat > chart.svg <<EOF
<svg xmlns="http://www.w3.org/2000/svg" width="400" height="400" viewBox="0 0 32 32">
  <circle r="16" cx="16" cy="16" fill="#2ecc71"
    stroke-dasharray="${passed} ${total}" stroke-width="32" />

  <circle r="16" cx="16" cy="16" fill="#e74c3c"
    stroke-dasharray="${failed} ${total}" stroke-dashoffset="-${passed}" stroke-width="32" />

  <circle r="16" cx="16" cy="16" fill="#f39c12"
    stroke-dasharray="${broken} ${total}" stroke-dashoffset="-${passed + failed}" stroke-width="32" />

  <circle r="16" cx="16" cy="16" fill="#95a5a6"
    stroke-dasharray="${skipped} ${total}" stroke-dashoffset="-${passed + failed + broken}" stroke-width="32" />

  <text x="16" y="16" text-anchor="middle" dominant-baseline="middle"
    font-size="3" fill="#000">
    ${successRate}%
  </text>
</svg>
EOF
        """

        // 3. send SVG as image
        sh """
            curl -s -X POST "https://api.telegram.org/bot\$BOT_TOKEN/sendPhoto" \
              -F chat_id=${chatId} \
              -F photo=@chart.svg \
              -F caption="📊 Test Results #${env.BUILD_NUMBER}"
        """

        // 4. zip Allure report
        sh """
            if command -v zip >/dev/null 2>&1; then
                zip -r allure-report.zip allure-report
            else
                tar -czf allure-report.tar.gz allure-report
            fi
        """

        // 5. send archive
        sh """
            if [ -f allure-report.zip ]; then
                FILE=allure-report.zip
            else
                FILE=allure-report.tar.gz
            fi

            curl -s -X POST "https://api.telegram.org/bot\$BOT_TOKEN/sendDocument" \
              -F chat_id=${chatId} \
              -F document=@\$FILE \
              -F caption="📊 Allure Report #${env.BUILD_NUMBER}"
        """
    }
}