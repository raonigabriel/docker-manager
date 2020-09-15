package com.github.raonigabriel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.transport.DockerHttpClient;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;

@Configuration
public class DockerConfig {

	@Bean
	public DockerClientConfig dockerClientConfig() {
		return DefaultDockerClientConfig.createDefaultConfigBuilder()
			    .withDockerHost("unix:///var/run/docker.sock")
//			    .withDockerHost("tcp://localhost:2735")
			    .withDockerTlsVerify(false).build();
	}
	
	@Bean
	public DockerHttpClient dockerHttpClient(DockerClientConfig dockerClientConfig) {
		return new ZerodepDockerHttpClient.Builder()
				.dockerHost(dockerClientConfig.getDockerHost()).build();
	}
	
	@Bean
	public DockerClient dockerClient(DockerClientConfig config, DockerHttpClient transport) {
		return DockerClientImpl.getInstance(config, transport);
	}

}
