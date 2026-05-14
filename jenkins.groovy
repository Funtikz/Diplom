task_branch = "${TEST_BRANCH_NAME}"
def branch_cutted = task_branch.contains("origin") ? task_branch.split('/')[1] : task_branch.trim()
currentBuild.displayName = "$branch_cutted"
base_git_url = "https://github.com/Funtikz/Diplom.git"

// Укажи chat_id своей Telegram-группы
def TELEGRAM_CHAT_ID = "-1003786359995"

node {
    withEnv(["branch=${branch_cutted}", "base_url=${base_git_url}"]) {

        stage("Checkout Branch") {
            if (!"$branch_cutted".contains("master")) {
                try {
                    getProject("$base_git_url", "$branch_cutted")
                } catch (err) {
                    echo "Failed get branch $branch_cutted"
                    throw("${err}")
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
                sendTelegramReport(TELEGRAM_CHAT_ID)
            }
        }
    }
}

def runTests() {
    try {
        labelledShell(
                label: "Run tests",
                script: """
                chmod +x gradlew
                ./gradlew clean test
            """
        )
    } finally {
        echo "Test execution finished (some tests may have failed)"
    }
}

def getProject(String repo, String branch) {
    cleanWs()
    checkout scm: [
            $class: 'GitSCM',
            branches: [[name: branch]],
            userRemoteConfigs: [[url: repo]]
    ]
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

def sendTelegramReport(String chatId) {
    // Если currentResult ещё не установлен, считаем сборку успешной
    def status = currentBuild.currentResult ?: "SUCCESS"
    def statusEmoji = status == "SUCCESS" ? "✅" : "❌"

    def allureUrl = "${env.BUILD_URL}allure/"

    def message = """
${statusEmoji} Jenkins Build Report

📦 Job: ${env.JOB_NAME}
🔢 Build: #${env.BUILD_NUMBER}
🌿 Branch: ${branch_cutted}
📌 Status: ${status}

📊 Allure Report:
${allureUrl}
"""

    // BOT_TOKEN должен быть создан в Jenkins Credentials
    // Kind: Secret text
    // ID: telegram-bot-token
    withCredentials([
            string(credentialsId: 'telegram-bot-token', variable: 'BOT_TOKEN')
    ]) {
        sh """
            curl -s -X POST "https://api.telegram.org/bot${BOT_TOKEN}/sendMessage" \
              -d "chat_id=${chatId}" \
              --data-urlencode "text=${message}"
        """
    }
}