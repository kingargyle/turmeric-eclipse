/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.eclipse.resources.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ebayopensource.turmeric.eclipse.core.resources.constants.SOAProjectConstants;
import org.ebayopensource.turmeric.eclipse.logging.SOALogger;
import org.ebayopensource.turmeric.eclipse.resources.model.SOAImplMetadata;
import org.ebayopensource.turmeric.eclipse.resources.model.SOAImplProject;
import org.ebayopensource.turmeric.eclipse.resources.model.SOAProjectEclipseMetadata;
import org.ebayopensource.turmeric.eclipse.resources.model.ISOAProject.SOAProjectSourceDirectory;
import org.ebayopensource.turmeric.eclipse.soatools.configtool.ConfigTool;
import org.ebayopensource.turmeric.eclipse.utils.plugin.JDTUtil;
import org.ebayopensource.turmeric.eclipse.utils.plugin.WorkspaceUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;


/**
 * @author smathew
 * 
 */
public class SOAImplUtil {
	private static final SOALogger logger = SOALogger.getLogger();
	
	public static IFile getServiceConfigFile(final SOAProjectEclipseMetadata metadata, final String serviceName) {
		if (SOALogger.DEBUG)
			logger.entering(metadata, serviceName);
		final IFile result = getServiceConfigFile(metadata.getProject(), serviceName);
		if (SOALogger.DEBUG)
			logger.exiting(result);
		return result;
	}
	
	public static IFile getServiceConfigFile(final IProject project, final String serviceName) {
		if (SOALogger.DEBUG)
			logger.entering(project, serviceName);
		
		final String serviceConfigFile = WorkspaceUtil.addPathSeperators(
				SOAProjectConstants.FOLDER_META_SRC,
				SOAProjectConstants.IMPL_SERVICE_CONFIG_DIR, serviceName,
				SOAProjectConstants.IMPL_SERVICE_CONFIG_XML);
		final IFile result = project.getFile(serviceConfigFile);
		if (SOALogger.DEBUG)
			logger.exiting(result);
		return result;
	}

	public static SOAImplProject fillMetadata(
			final SOAProjectEclipseMetadata eclipseMetadata)
			throws IOException, CoreException {
		if (SOALogger.DEBUG)
			logger.entering(eclipseMetadata);
		SOAImplProject implProject = new SOAImplProject();
		implProject.setEclipseMetadata(eclipseMetadata);
		SOAImplMetadata metadata = SOAServiceUtil
				.getSOAImplMetadata(eclipseMetadata);
		implProject.setMetadata(metadata);
		if (SOALogger.DEBUG)
			logger.exiting(implProject);
		return implProject;
	}
	
	public static SOAImplMetadata loadServiceConfig(final IProject implProject, final String serviceName) throws Exception {
		if (SOALogger.DEBUG)
			logger.entering(implProject, serviceName);
		SOAImplMetadata result = SOAImplMetadata.create(implProject.getName(), 
				SOAProjectConstants.DEFAULT_BASE_CONSUMER_SOURCE_DIRECTORY);
		final IFile serviceConfigFile = getServiceConfigFile(implProject.getProject(), 
				serviceName);
		if (serviceConfigFile.isAccessible()) {
			ConfigTool.parseServiceConfig(serviceConfigFile.getContents(), result);
		} else {
			logger.warning("The service config file is either not exist or not accessible->"
					, serviceConfigFile.getLocation());
		}
		if (SOALogger.DEBUG)
			logger.exiting(result);
		return result;
	}
	
	public static void loadServiceConfig(final SOAImplProject implProject, final String serviceName) throws Exception {
		if (SOALogger.DEBUG)
			logger.entering(implProject, serviceName);
		
		final IFile serviceConfigFile = getServiceConfigFile(implProject.getProject(), 
				serviceName);
		if (serviceConfigFile.isAccessible()) {
			ConfigTool.parseServiceConfig(serviceConfigFile.getContents(), implProject.getMetadata());
		} else {
			logger.warning("The service config file is either not exist or not accessible->"
					, serviceConfigFile.getLocation());
		}
		
		if (SOALogger.DEBUG)
			logger.exiting();
	}

	public static SOAImplProject fillMetadata(
			final SOAProjectEclipseMetadata eclipseMetadata,
			final SOAImplProject implProject) throws Exception {
		if (SOALogger.DEBUG)
			logger.entering(eclipseMetadata, implProject);
		final List<SOAProjectSourceDirectory> entries = new ArrayList<SOAProjectSourceDirectory>();
		for (final IClasspathEntry entry : JDTUtil.rawClasspath(implProject
				.getProject(), false)) {
			if (entry != null && entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				final String path = entry.getPath().removeFirstSegments(1)
				.toString();
				entries.add(new SOAProjectSourceDirectory(path));
			}
		}
		if (SOALogger.DEBUG)
			logger.debug("Setting source directories->", entries, " to the impl project->", 
					implProject.getProject());
		implProject.setSourceDirectories(entries);
		
		final String serviceName = implProject.getMetadata().getServiceName();
		if (serviceName != null) {
			final IProject intfProject = WorkspaceUtil.getProject(serviceName);
			if (intfProject != null && intfProject.isAccessible()) {
				SOAServiceUtil.getSOAIntfMetadata(SOAServiceUtil
								.getSOAEclipseMetadata(intfProject), implProject.getMetadata().getIntfMetadata());
				SOAIntfUtil.fillMetadata(intfProject, implProject.getMetadata().getIntfMetadata());
			} else {
				logger.warning("The interface project is not accessible->", intfProject);
			}
		} else {
			if (SOALogger.DEBUG)
				logger.debug("The interface project/service name for the implementation project", 
						implProject.getProject(), " is null->");
		}
		return implProject;

	}

	public static IFolder getGenWebFolder(IProject project) {
		return project.getFolder(SOAProjectConstants.FOLDER_GEN_WEB_CONTENT);
	}

	public static IFolder getTempGenWebFolder(IProject project) {
		return project.getFolder(SOAProjectConstants.TEMP_PREFIX
				+ SOAProjectConstants.FOLDER_GEN_WEB_CONTENT);
	}

	public static IFile getJarFile(IProject project) {
		return project.getFile(project.getName() + SOAProjectConstants.JAR_EXT);
	}

	public static IFile getWarFile(final IProject implProject, final String serviceName) {
		return implProject.getFile(serviceName + SOAProjectConstants.WAR_EXT);
	}
	
	public static IFile getServiceImplPropertiesFile(IProject implProject){
		return implProject.getFile(SOAProjectConstants.PROPS_FILE_SERVICE_IMPL);
	}
	
	public static IFile getServiceImplWebXMLFile(IProject implProject){
		return implProject.getFile(SOAProjectConstants.FOLDER_GEN_WEB_CONTENT
				+ SOAProjectConstants.DELIMITER_URL_SLASH
				+ SOAProjectConstants.FOLDER_WEB_INF
				+ SOAProjectConstants.DELIMITER_URL_SLASH
				+ SOAProjectConstants.FILE_WEB_XML);
	}
	
	/*public static String getImplNameFromServiceName(String serviceName){
		return serviceName+SOAProjectConstants.IMPL_PROJECT_SUFFIX;
	}*/
}
