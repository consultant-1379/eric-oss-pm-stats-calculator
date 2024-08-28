# Maven settings.xml
After you downloaded the settings.xml file you should put it into the **_C:\Users\SIGNUM\\.m2_** folder as settings.xml
> **_NOTE:_** Here you can find the [settings.xml](https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/IDUN/Setup+development+environment?preview=/497784106/508925150/settings.xml)

> **_NOTE:_** Learn more about maven settings [here](https://maven.apache.org/settings.html).

# Unit Test
In order to run the unit test locally, you should be in the base directory where you can find the parent pom.
If you wish to run a submodule's unit test separately you need to enter into the specific directory and run it there.
```
mvn test
```
> **_NOTE:_** If you modify your files and it's not compiled yet then you should run `mvn clean install`

> **_NOTE:_** You can skip the tests with -DskipTests flag

> **_NOTE:_** [If you wish to learn more about Maven](https://maven.apache.org/)

# Run Integration Test

In order to run the Integration Test locally run the `localHelmTestScript.sh` script.

```shell
eszbzlt@HU-00003306 MINGW64 /c/Git/idun/eric-oss-pm-stats-calculator/helm-test-local (master)
$ ./localHelmTestScript.sh
Do you wish to run Integration Tests on ECCD/KaaS server (Yes/No)? [y/n] ? n
No
Do you want this script to clean the environment after (Yes/No)? [y/n] ? n
No
Do you want to initialize Helm repositories (Yes/No)? [y/n] ? n
No
```

> **_NOTE:_** Don't forget to set the `SELI_ARTIFACTORY_REPO_USER` and `SELI_ARTIFACTORY_REPO_PASS` environment variable, before runing the IT test\
> To set set these, you can follow the steps of **Generate API Key** section on [Common OSS Artifactory Repositories][CommonOSSArtifactoryRepositories] or check out the official documentation [Artifactory User Guide - Creating an API Key](ArtifactoryUserGuide-CreatinganAPIKey)\
> It's also recommended to set these variables globally by adding them to your bash profile (only needed once)\
> `echo "export SELI_ARTIFACTORY_REPO_USER=<your signum>" >> ~/.bash_profile; echo "export SELI_ARTIFACTORY_REPO_PASS=<your API key>" >> ~/.bash_profile`

> **_NOTE:_** You have to call the localHelmTestScript.sh from the _helm-test-local_ folder otherwise it won't work.

> **_NOTE:_** The `localHelmTestScript.sh` is not going to execute unit tests.

> **_NOTE:_** The original intention with `localHelmTestScript.sh` script is to apply fail-fast mechanism.
If the script is not working according to this then [contact us](mailto:PDLVELOCIR@pdl.internal.ericsson.com).

> **_NOTE:_** Sometimes the helm repositories change. To make it up to date you should initialize it at the first start.

> **_TIP:_** The `initRepo` step takes much time if your local `Helm` repository contains many artifacts.
If your charts are initialized already - dependency charts are available under `/charts` folder - , then you can skip `initialize Helm repositories` step.

> **_TIP:_** To delete test images run:
> ```shell
> docker rmi -f $(docker image ls --format '{{.Repository}}:{{.Tag}}' | grep armdocker.rnd.ericsson.se/proj-eric-oss-dev/eric-oss-pm-stats-calculator*)
> ```

> **_Troubleshooting:_** In case of an IT test failure check the namespace for all the pods running. IT tests require high resources, make sure all the pods could start. If you see in db-import pod logs like 'cannot interpret /bin/bash^M' then make sure that the scripts in the docker images use linux line endings and not window's
---

## <a name="StartDashboard">Start Dashboard</a>

### Example
```bash
bash start_kubernetes_dashboard.sh chrome
```
The argument for this script is the browser you want it to open, eg chrome.

The token secret will be written to a text file in helm-test-local folder and stdout.
Use this token to log into the dashboard.

[Dashboard link](http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/.)

[CommonOSSArtifactoryRepositories]: https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/DGBase/Common+OSS+Artifactory+Repositories
[ArtifactoryUserGuide-CreatinganAPIKey]: https://www.jfrog.com/confluence/display/JFROG/User+Profile#UserProfile-CreatinganAPIKey