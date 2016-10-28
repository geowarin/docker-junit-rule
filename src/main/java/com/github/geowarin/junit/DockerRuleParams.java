package com.github.geowarin.junit;

import com.spotify.docker.client.messages.ContainerConfig;

public class DockerRuleParams {

  String imageName;

  String[] ports;
  String cmd;

  String portToWaitOn;
  public int waitTimeout;
  String logToWait;

  ContainerConfig config;
  String dockerHost;
  String dockerCertPath;

}
