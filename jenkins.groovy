import groovy.json.JsonSlurper

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
                getProject(base_git_url, branch_cutted)
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

            stage("Telegram") {
                sendTelegramReport(TELEGRAM_CHAT_ID, branch_cutted)
            }
        }
    }
}

def runTests() {
    sh """
        chmod +x gradlew
        ./gradlew clean test
    """
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
        def json = new JsonSlurper().parseText(readFile(summaryFile))
        passed = json.statistic.passed ?: 0
        failed = json.statistic.failed ?: 0
        broken = json.statistic.broken ?: 0
        skipped = json.statistic.skipped ?: 0
        total = json.statistic.total ?: 0
    }

    def status = currentBuild.currentResult ?: "SUCCESS"
    def emoji = status == "SUCCESS" ? "✅" : status == "FAILURE" ? "❌" : "⚠️"

    def successRate = total > 0 ? (passed * 100.0 / total).round(1) : 0

    def message = """
${emoji} Jenkins Report

📦 ${env.JOB_NAME}
🔢 #${env.BUILD_NUMBER}
🌿 ${branchName}

✅ Passed: ${passed}
❌ Failed: ${failed}
💥 Broken: ${broken}
⏭ Skipped: ${skipped}
📦 Total: ${total}
📈 Success: ${successRate}%
"""

    withCredentials([string(credentialsId: 'telegram-bot-token', variable: 'BOT_TOKEN')]) {

        // 1. TEXT
        sh """
            curl -s -X POST "https://api.telegram.org/bot\$BOT_TOKEN/sendMessage" \
              -d "chat_id=${chatId}" \
              --data-urlencode "text=${message}"
        """

        // 2. SVG PIE CHART (NO dependencies)
        sh """
cat > chart.svg <<EOF
<svg xmlns="http://www.w3.org/2000/svg" width="300" height="300">
  <circle r="80" cx="150" cy="150" fill="none"
    stroke="#2ecc71" stroke-width="40"
    stroke-dasharray="${passed} ${total}" />

  <circle r="80" cx="150" cy="150" fill="none"
    stroke="#e74c3c" stroke-width="40"
    stroke-dasharray="${failed} ${total}"
    stroke-dashoffset="-${passed}" />

  <circle r="80" cx="150" cy="150" fill="none"
    stroke="#f39c12" stroke-width="40"
    stroke-dasharray="${broken} ${total}"
    stroke-dashoffset="-${passed + failed}" />

  <text x="150" y="155" text-anchor="middle" font-size="20" fill="black">
    ${successRate}%
  </text>
</svg>
EOF
        """

        // 3. SEND SVG (IMPORTANT: as document, not photo)
        sh """
            curl -s -X POST "https://api.telegram.org/bot\$BOT_TOKEN/sendDocument" \
              -F chat_id=${chatId} \
              -F document=@chart.svg \
              -F caption="📊 Test Chart #${env.BUILD_NUMBER}"
        """

        // 4. archive allure
        sh """
            tar -czf allure-report.tar.gz allure-report
        """

        // 5. send report
        sh """
            curl -s -X POST "https://api.telegram.org/bot\$BOT_TOKEN/sendDocument" \
              -F chat_id=${chatId} \
              -F document=@allure-report.tar.gz \
              -F caption="📊 Allure #${env.BUILD_NUMBER}"
        """
    }
}


