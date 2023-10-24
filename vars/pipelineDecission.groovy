#!groovy

def decidePipeline(Map configMap){
    application = configMap.get("application")
    //Here we are getting nodeJSVM
    switch(application) {
        case 'nodeJSVM':
            echo "application is Node JS and VM based"
            nodeJSVMCI.call(configMap)
           break
        case 'JavaVM':
           JavaVMCI.call(configMap)
           break
        default:
           error "Un recognised application"
           break
    }
}