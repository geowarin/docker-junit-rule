package com.github.geowarin.junit;

public class DockerRuleParams {

  String imageName;

  String[] ports;
  String cmd;

  String portToWaitOn;
  public int waitTimeout;
  String logToWait;

  Integer waitBeforeProceed;
}
