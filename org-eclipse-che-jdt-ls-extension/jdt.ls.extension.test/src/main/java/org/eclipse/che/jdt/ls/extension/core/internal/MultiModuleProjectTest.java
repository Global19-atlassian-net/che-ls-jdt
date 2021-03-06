/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.jdt.ls.extension.core.internal;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.Severity;
import org.eclipse.che.jdt.ls.extension.api.dto.ClasspathEntry;
import org.eclipse.che.jdt.ls.extension.api.dto.ExternalLibrariesParameters;
import org.eclipse.che.jdt.ls.extension.api.dto.Jar;
import org.eclipse.che.jdt.ls.extension.api.dto.JobResult;
import org.eclipse.che.jdt.ls.extension.api.dto.UpdateWorkspaceParameters;
import org.eclipse.che.jdt.ls.extension.core.internal.classpath.ResolveClassPathsHandler;
import org.eclipse.che.jdt.ls.extension.core.internal.externallibrary.ProjectExternalLibraryCommand;
import org.eclipse.che.jdt.ls.extension.core.internal.pom.GetMavenProjectsCommand;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;
import org.junit.Test;

/** @author Anatolii Bazko */
public class MultiModuleProjectTest extends AbstractProjectsManagerBasedTest {

  @Before
  public void setUp() throws Exception {
    URI addedProjectUri = copyFiles("maven/multimodule", true).toURI();

    UpdateWorkspaceParameters parameters = new UpdateWorkspaceParameters();
    parameters.setAddedProjectsUri(singletonList(addedProjectUri.toString()));

    JobResult jobResult =
        UpdateWorkspaceCommand.execute(singletonList(parameters), new NullProgressMonitor());

    assertEquals(jobResult.getSeverity(), Severity.OK);
  }

  @Test
  public void shouldReturnValidMavenModulesForParentModule() throws Exception {
    File rootModule = new File(getWorkingProjectDirectory(), "maven/multimodule");
    assertTrue(rootModule.exists());

    List<String> projectsUri =
        GetMavenProjectsCommand.execute(
            singletonList(rootModule.toURI().toString()), new NullProgressMonitor());

    assertEquals(2, projectsUri.size());
  }

  @Test
  public void shouldNotReturnClasspathsEntriesForParentModule() throws Exception {
    File rootModule = new File(getWorkingProjectDirectory(), "maven/multimodule");
    assertTrue(rootModule.exists());

    List<ClasspathEntry> classpathEntries =
        ResolveClassPathsHandler.getClasspathModelTree(
            singletonList(rootModule.toURI().toString()), new NullProgressMonitor());

    assertTrue(classpathEntries.isEmpty());
  }

  @Test
  public void shouldReturnDefaultLibrariesForParentModule() throws Exception {
    File rootModule = new File(getWorkingProjectDirectory(), "maven/multimodule");
    assertTrue(rootModule.exists());

    ExternalLibrariesParameters params =
        new ExternalLibrariesParameters(rootModule.toURI().toString());
    List<Jar> libs =
        ProjectExternalLibraryCommand.execute(singletonList(params), new NullProgressMonitor());

    assertTrue(!libs.isEmpty());
  }

  @Test
  public void shouldReturnExternalLibrariesForSubModule() throws Exception {
    File myLibModule = new File(getWorkingProjectDirectory(), "maven/multimodule/my-lib");
    assertTrue(myLibModule.exists());

    ExternalLibrariesParameters params =
        new ExternalLibrariesParameters(myLibModule.toURI().toString());
    List<Jar> libs =
        ProjectExternalLibraryCommand.execute(singletonList(params), new NullProgressMonitor());

    assertTrue(!libs.isEmpty());
  }
}
