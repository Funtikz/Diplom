task_branch = "${TEST_BRANCH_NAME}"
def branch_cutted = task_branch.contains("origin") ? task_branch.split('/')[1] : task_branch.trim()
currentBuild.displayName = "$branch_cutted"
base_git_url = "https://github.com/Funtikz/Diplom.git"

node {
    withEnv(["branch=${branch_cutted}", "base_url=${base_git_url}"]) {
        stage("Checkout Branch") {
            if (!"$branch_cutted".contains("master")) {
                try {
                    getProject("$base_git_url", "$branch_cutted")
                } catch (err) {
                    echo "Failed get branch $branch_cutted"
                    throw ("${err}")
                }
            } else {
                echo "Current branch is master"
            }
        }

        try {
            stage("Запуск тестов") {
                parallel getTestStages(["apiTests", "uiTests"])
            }
        } finally {
            stage("Allure") {
                generateAllure()
            }
        }
    }
}

def getTestStages(testTypes) {
    def stages = [:]
    testTypes.each { type ->
        stages["${type}"] = {
            runTests()
        }
    }
    return stages
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