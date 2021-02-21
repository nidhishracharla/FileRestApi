node{
stage('project checkout'){
  git branch: 'main', poll: false, url: 'https://github.com/nidhishracharla/FileRestApi.git'
}
stage('compile-package'){
    bat 'mvn install'
} 
  
}
