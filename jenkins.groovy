import groovy.json.JsonSlurper

def task_branch = "${TEST_BRANCH_NAME}"
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

        stage("Checkout") {
            cleanWs()

            checkout([
                    $class: 'GitSCM',
                    branches: [[name: branch_cutted]],
                    userRemoteConfigs: [[url: base_git_url]]
            ])
        }

        try {
            stage("Tests") {
                sh """
                    chmod +x gradlew
                    ./gradlew clean test
                """
            }
        } finally {

            stage("Allure") {
                allure([
                        includeProperties: true,
                        reportBuildPolicy: 'ALWAYS',
                        results: [[path: 'build/allure-results']]
                ])
            }

            stage("Telegram Report") {
                sendTelegramReport(TELEGRAM_CHAT_ID, branch_cutted)
            }

            stage("Email Report") {
                sendEmailReport(branch_cutted)
            }
        }
    }
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
        def json = new JsonSlurper().parseText(jsonText)

        passed = json.statistic.passed ?: 0
        failed = json.statistic.failed ?: 0
        broken = json.statistic.broken ?: 0
        skipped = json.statistic.skipped ?: 0
        total = json.statistic.total ?: 0
    }

    def status = currentBuild.currentResult ?: "SUCCESS"
    def emoji = status == "SUCCESS" ? "✅"
            : status == "FAILURE" ? "❌"
            : "⚠️"

    def successRate = total > 0
            ? String.format('%.1f', (passed * 100.0 / total))
            : "0.0"

    def message = """
${emoji} Jenkins Report

📦 Job: ${env.JOB_NAME}
🔢 Build: #${env.BUILD_NUMBER}
🌿 Branch: ${branchName}

📊 Passed: ${passed}
❌ Failed: ${failed}
💥 Broken: ${broken}
⏭ Skipped: ${skipped}
📦 Total: ${total}

📈 Success: ${successRate}%
"""

    withCredentials([string(credentialsId: 'telegram-bot-token', variable: 'BOT_TOKEN')]) {

        // 1. Text message
        sh """
            curl -s -X POST "https://api.telegram.org/bot\$BOT_TOKEN/sendMessage" \
              -d "chat_id=${chatId}" \
              --data-urlencode "text=${message}"
        """

        // 2. SVG chart
        sh """
cat > chart.svg <<EOF
<svg xmlns="http://www.w3.org/2000/svg" width="420" height="240">
  <rect width="420" height="240" fill="#ffffff"/>

  <text x="20" y="40" font-size="20">Test Results</text>

  <text x="20" y="80" font-size="16" fill="green">Passed: ${passed}</text>
  <text x="20" y="110" font-size="16" fill="red">Failed: ${failed}</text>
  <text x="20" y="140" font-size="16" fill="orange">Broken: ${broken}</text>
  <text x="20" y="170" font-size="16" fill="gray">Skipped: ${skipped}</text>

  <text x="20" y="210" font-size="18">
    Success Rate: ${successRate}%
  </text>
</svg>
EOF
        """

        // 3. Send image
        sh """
            curl -s -X POST "https://api.telegram.org/bot\$BOT_TOKEN/sendPhoto" \
              -F chat_id=${chatId} \
              -F photo=@chart.svg \
              -F caption="📊 Build #${env.BUILD_NUMBER}"
        """

        // 4. Archive Allure report
        sh """
            if command -v zip >/dev/null 2>&1; then
                zip -r allure-report.zip allure-report
            else
                tar -czf allure-report.tar.gz allure-report
            fi
        """

        // 5. Send archive
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

def sendEmailReport(String branchName) {

    def summaryFile = "allure-report/widgets/summary.json"

    def passed = 0
    def failed = 0
    def broken = 0
    def skipped = 0
    def total = 0

    if (fileExists(summaryFile)) {
        def jsonText = readFile(summaryFile)
        def json = new JsonSlurper().parseText(jsonText)

        passed = json.statistic.passed ?: 0
        failed = json.statistic.failed ?: 0
        broken = json.statistic.broken ?: 0
        skipped = json.statistic.skipped ?: 0
        total = json.statistic.total ?: 0
    }

    def status = currentBuild.currentResult ?: "SUCCESS"
    def successRate = total > 0 ? (passed * 100.0 / total) : 0

    def color = status == "SUCCESS" ? "green"
            : status == "FAILURE" ? "red"
            : "orange"

    emailext(
            to: "228funtikx@gmail.com",
            subject: "Jenkins Build #${env.BUILD_NUMBER} - ${status}",
            body: """
<html>
<body>
<h2 style="color:${color}">Jenkins Test Report</h2>

<p><b>Project:</b> ${env.JOB_NAME}</p>
<p><b>Branch:</b> ${branchName}</p>
<p><b>Build:</b> #${env.BUILD_NUMBER}</p>
<p><b>Status:</b> ${status}</p>

<hr>

<h3>Test Results</h3>
<ul>
<li>Passed: ${passed}</li>
<li>Failed: ${failed}</li>
<li>Broken: ${broken}</li>
<li>Skipped: ${skipped}</li>
<li>Total: ${total}</li>
</ul>

<p><b>Success rate:</b> ${String.format('%.1f', successRate)}%</p>

<hr>

<p>🔗 <a href="${env.BUILD_URL}">Open Jenkins Build</a></p>

</body>
</html>
"""
    )
}