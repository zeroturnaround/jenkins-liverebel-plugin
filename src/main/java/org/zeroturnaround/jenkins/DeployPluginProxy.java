package org.zeroturnaround.jenkins;

/*****************************************************************
Copyright 2011 ZeroTurnaround OÜ

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*****************************************************************/

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.plugins.deploy.ContainerAdapter;

import java.io.IOException;

/**
 * @author Juri Timoshin
 */
public class DeployPluginProxy {

	private final ContainerAdapter adapter;
	private final AbstractBuild build;
	private final Launcher launcher;
	private final BuildListener listener;

	public DeployPluginProxy(ContainerAdapter adapter, AbstractBuild build, Launcher launcher, BuildListener listener) {
		this.adapter = adapter;
		this.build = build;
		this.launcher = launcher;
		this.listener = listener;
	}

	public boolean cargoDeploy(FilePath warFile) throws IOException, InterruptedException {
		listener.getLogger().println("Deploying new artifact without LiveRebel...");
		return adapter.redeploy(warFile, build, launcher, listener);
	}
}
