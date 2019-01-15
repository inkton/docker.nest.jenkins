#!groovy
 
import jenkins.model.*
import hudson.security.*
import jenkins.security.s2m.AdminWhitelistRule
 
def instance = Jenkins.getInstance()
 
def hudsonRealm = new HudsonPrivateSecurityRealm(false)

def env = System.getenv()

hudsonRealm.createAccount(env['NEST_APP_TAG'], env['NEST_SERVICES_PASSWORD'])
instance.setSecurityRealm(hudsonRealm)
 
def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
instance.setAuthorizationStrategy(strategy)
instance.save()
 
Jenkins.instance.getInjector().getInstance(AdminWhitelistRule.class).setMasterKillSwitch(false)

"addgroup -g 1008 tree".execute().waitFor()

"mkdir /root/.ssh".execute().waitFor()

byte[] b1 = env['NEST_CONTACT_KEY'].decodeBase64();
byte[] b2 = env['NEST_TREE_KEY'].decodeBase64();

FileOutputStream out1 = new FileOutputStream(new File("/root/.ssh/id_rsa"))
out1.write(b1)
out1.close()

FileOutputStream out2 = new FileOutputStream(new File("/root/.ssh/known_hosts"))
out2.write(b2)
out2.close()

"chmod -R 600 /root/.ssh".execute().waitFor()

def proc = "ssh-keygen -f /root/.ssh/id_rsa -y".execute()
FileOutputStream out3 = new FileOutputStream(new File("/root/.ssh/id_rsa.pub"))
proc.waitForProcessOutput(out3, System.err)
proc.waitFor()

"cat /root/.ssh/id_rsa.pub /root/.ssh/authorized_keys".execute().waitFor()

FileOutputStream out4 = new FileOutputStream(new File("/root/.ssh/config"))
PrintStream printStream = new PrintStream(out4);
printStream.print("Host nest\n\tHostName "+env['NEST_APP_TAG']+".nestapp.yt\n\tUser "+env['NEST_CONTACT_ID']+"\nHost *\n\tStrictHostKeyChecking no\n\tCheckHostIP no\n")

printStream.close();

