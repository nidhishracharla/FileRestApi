node{
stage('project checkout'){
  git branch: 'main', poll: false, url: 'https://github.com/nidhishracharla/FileRestApi.git'
}
stage('compile-package'){
    def mavenhome= tool name: 'MyMaven', type: 'maven'
  bat "${mavenhome}/bin/mvn install DskipTests"
} 
  
}
