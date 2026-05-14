import groovy.json.JsonSlurperClassic

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
        def json = new JsonSlurperClassic().parseText(readFile(summaryFile))

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
            ? String.format("%.1f", (passed * 100.0 / total))
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

        // 1. send text
        sh """
            curl -s -X POST "https://api.telegram.org/bot\$BOT_TOKEN/sendMessage" \
              -d "chat_id=${chatId}" \
              --data-urlencode "text=${message}"
        """

        // 2. generate REAL image using HTML (no libs needed)
        sh """
cat > report.html <<EOF
<html>
<head>
<meta charset="UTF-8">
</head>
<body style="font-family: Arial; text-align:center;">

<h2>Jenkins Test Report</h2>

<p>Passed: ${passed}</p>
<p>Failed: ${failed}</p>
<p>Broken: ${broken}</p>
<p>Skipped: ${skipped}</p>

<h3>Success Rate: ${successRate}%</h3>

</body>
</html>
EOF
        """

        // 3. render image using Playwright (SAFE WAY)
        sh """
node -e "
const fs = require('fs');
const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({ args: ['--no-sandbox'] });
  const page = await browser.newPage();

  const html = fs.readFileSync('report.html', 'utf8');
  await page.setContent(html);

  await page.screenshot({ path: 'report.png' });

  await browser.close();
})();
"
        """

        // 4. send image
        sh """
            curl -s -X POST "https://api.telegram.org/bot\$BOT_TOKEN/sendPhoto" \
              -F chat_id=${chatId} \
              -F photo=@report.png \
              -F caption="📊 Build #${env.BUILD_NUMBER}"
        """
    }
}