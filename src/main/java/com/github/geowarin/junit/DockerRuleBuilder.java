package com.github.geowarin.junit;

public class DockerRuleBuilder {
  private final DockerRuleParams params = new DockerRuleParams();

  public DockerRuleBuilder image(String imageName) {
    params.imageName = imageName;
    return this;
  }

  public DockerRuleBuilder ports(String... ports) {
    params.ports = ports;
    return this;
  }

  public DockerRuleBuilder cmd(String cmd) {
    params.cmd = cmd;
    return this;
  }

  public DockerRuleBuilder waitForPort(String portToWaitOn) {
    params.portToWaitOn = portToWaitOn;
    return this;
  }

  public DockerRuleBuilder waitForLog(String logToWait) {
    params.logToWait = logToWait;
    return this;
  }

  public DockerRule build() {
    return new DockerRule(params);
  }
}
