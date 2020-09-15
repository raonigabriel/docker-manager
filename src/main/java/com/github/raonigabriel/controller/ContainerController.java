package com.github.raonigabriel.controller;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.SocketUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.api.command.RemoveContainerCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;

@RestController
@RequestMapping("/containers")
public class ContainerController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContainerController.class); 

	@Autowired
	DockerClient dockerClient;

	@GetMapping
	public ResponseEntity<Collection<Container>> listAll() {
		// This is the equivalent of running "docker ps -a"
		try (ListContainersCmd cmd = dockerClient.listContainersCmd().withShowAll(true)) {
			return ResponseEntity.ok(cmd.exec());
		} catch (Exception ex) {
			LOGGER.error("Failed to retrieve container list", ex);
			return ResponseEntity.status(500).build();
		}
	}

	@GetMapping
	public ResponseEntity<Container> getById(@PathVariable String containerId) {
		List<String> filter = Collections.singletonList(containerId);
		try (ListContainersCmd cmd = dockerClient.listContainersCmd().withIdFilter(filter)) {
			List<Container> results = cmd.exec();
			if (results.isEmpty()) {
				return ResponseEntity.notFound().build();
			} else {
				return ResponseEntity.ok(results.get(0));
			}
		} catch (Exception ex) {
			LOGGER.error("Failed to retrieve container", ex);
			return ResponseEntity.status(500).build();
		}
	}


	@DeleteMapping
	public ResponseEntity<Void> deleteContainerById(@PathVariable String containerId) {
		try (RemoveContainerCmd cmd = dockerClient.removeContainerCmd(containerId)) {
			cmd.exec();
		} catch (Exception ex) {
			LOGGER.error("Failed to delete container", ex);
		}
		return ResponseEntity.noContent().build();
	}

	@PostMapping
	public ResponseEntity<Container> createNew() {

		int freePort = SocketUtils.findAvailableTcpPort(9000, 9100);
		HostConfig hostConfig = HostConfig.newHostConfig().withAutoRemove(true)
				.withPortBindings(PortBinding.parse(freePort + ":8080"));

		String image = "raonigabriel/spring-qrcode-example";

		try (CreateContainerCmd cmd = dockerClient.createContainerCmd(image).withHostConfig(hostConfig)) {
			String containerId = cmd.exec().getId();
			dockerClient.startContainerCmd(containerId).exec();
			List<String> filter = Collections.singletonList(containerId);
			return ResponseEntity.ok(dockerClient.listContainersCmd().withIdFilter(filter).exec().get(0));
		} catch (Exception ex) {
			LOGGER.error("Failed to create/start container", ex);
			return ResponseEntity.status(500).build();
		}
	}
}
