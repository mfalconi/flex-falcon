/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.flex.compiler.internal.codegen.js.flexjs;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;
import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.exceptions.ConfigurationException;
import org.apache.flex.compiler.internal.codegen.js.goog.TestGoogProject;
import org.apache.flex.compiler.internal.config.TargetSettings;
import org.apache.flex.compiler.internal.driver.js.flexjs.FlexJSBackend;
import org.apache.flex.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.utils.FilenameNormalization;
import org.apache.flex.utils.ITestAdapter;
import org.apache.flex.utils.TestAdapterFactory;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This class tests the production of valid 'flexjs' JS code from an external
 * project.
 * 
 * @author Erik de Bruin
 */
public class TestFlexJSProject extends TestGoogProject
{
    private static ITestAdapter testAdapter = TestAdapterFactory.getTestAdapter();

    private static String projectDirPath = "flexjs/projects";
    private String sourcePath;
    private Collection<String> externs = new ArrayList<String>();

    @Override
    public void setUp()
    {
        backend = createBackend();
        project = new FlexJSProject(workspace, backend);
        project.config = new JSGoogConfiguration();
        super.setUp();
    }
    
    @Ignore
    @Test
    public void test_imports()
    {
        // crude bypass to allow for successful inheritance
    }

    @Test
    public void test_Test()
    {
        String testDirPath = projectDirPath + "/interfaces";

        String fileName = "Test";

        sourcePath = new File(TestAdapterFactory.getTestAdapter().getUnitTestBaseDir(),
                projectDirPath + "/interfaces").getPath();
        
        List<String> compiledFileNames = compileProject(fileName, testDirPath);

        assertProjectOut(compiledFileNames, testDirPath);
    }
    @Test
    public void test_Super()
    {
        String testDirPath = projectDirPath + "/super";

        String fileName = "Base";

        sourcePath = new File(TestAdapterFactory.getTestAdapter().getUnitTestBaseDir(),
                projectDirPath + "/super").getPath();

        List<String> compiledFileNames = compileProject(fileName, testDirPath);

        assertProjectOut(compiledFileNames, testDirPath);
    }

    @Test
    public void test_InternalAndSamePackageRequires()
    {
        String testDirPath = projectDirPath + "/internal";

        String fileName = "MainClass";

        sourcePath = new File(TestAdapterFactory.getTestAdapter().getUnitTestBaseDir(),
                projectDirPath + "/internal").getPath();

        List<String> compiledFileNames = compileProject(fileName, testDirPath);

        assertProjectOut(compiledFileNames, testDirPath);
    }

    @Test
    public void test_IsItCircular()
    {
        String testDirPath = projectDirPath + "/circular";

        String fileName = "Base";

        sourcePath = new File(TestAdapterFactory.getTestAdapter().getUnitTestBaseDir(),
                projectDirPath + "/circular").getPath();

        List<String> compiledFileNames = compileProject(fileName, testDirPath);

        assertProjectOut(compiledFileNames, testDirPath);
    }

    @Test
    public void test_XMLRequires()
    {
        String testDirPath = projectDirPath + "/xml_requires";

        String fileName = "XMLRequire";

        sourcePath = new File(TestAdapterFactory.getTestAdapter().getUnitTestBaseDir(),
                projectDirPath + "/xml_requires").getPath();

        List<String> compiledFileNames = compileProject(fileName, testDirPath);

        assertProjectOut(compiledFileNames, testDirPath);
    }

    @Test
    public void test_Overrides()
    {
        String testDirPath = projectDirPath + "/overrides";

        String fileName = "Test";

        try {
			((FlexJSProject)project).config.setCompilerAllowSubclassOverrides(null, true);
		} catch (ConfigurationException e) {
            Assert.fail(e.getMessage());
		}
        project.setTargetSettings(new TargetSettings(((FlexJSProject)project).config));
        
        sourcePath = new File(TestAdapterFactory.getTestAdapter().getUnitTestBaseDir(),
                projectDirPath + "/overrides").getPath();

        StringBuilder sb = new StringBuilder();
        List<String> compiledFileNames = compileProject(fileName, testDirPath, sb, false);

        assertProjectOut(compiledFileNames, testDirPath);
    }

    @Test
    public void test_Bad_Overrides()
    {
        String testDirPath = projectDirPath + "/bad_overrides";

        String fileName = "Test";

        try {
			((FlexJSProject)project).config.setCompilerAllowSubclassOverrides(null, true);
		} catch (ConfigurationException e) {
            Assert.fail(e.getMessage());
		}
        project.setTargetSettings(new TargetSettings(((FlexJSProject)project).config));
        
        sourcePath = new File(TestAdapterFactory.getTestAdapter().getUnitTestBaseDir(),
                projectDirPath + "/bad_overrides").getPath();

        StringBuilder sb = new StringBuilder();
        compileProject(fileName, testDirPath, sb, false);

        String out = sb.toString();
        out = out.replace("\\", "/");

        String expected = testAdapter.getUnitTestBaseDir().getPath() + "/flexjs/projects/bad_overrides/Test.as(31:29)\n" +
                "interface method someFunction in interface IA is implemented with an incompatible signature in class Test\n" +
                testAdapter.getUnitTestBaseDir().getPath() + "/flexjs/projects/bad_overrides/Test.as(36:26)\n" +
                "interface method someOtherFunction in interface IA is implemented with an incompatible signature in class Test\n" +
                testAdapter.getUnitTestBaseDir().getPath() + "/flexjs/projects/bad_overrides/Test.as(31:29)\n" +
                "Incompatible override.\n" +
                testAdapter.getUnitTestBaseDir().getPath() + "/flexjs/projects/bad_overrides/Test.as(36:26)\n" +
                "Incompatible override.\n";
        assertThat(out, is(expected));
    }
    
    @Test
    public void test_PackageConflict_AmbiguousDefinition()
    {
        String testDirPath = projectDirPath + "/package_conflicts_ambiguous_definition";

        String fileName = "AmbiguousDefinition";

        externs.add("Event");

        sourcePath = new File(TestAdapterFactory.getTestAdapter().getUnitTestBaseDir(),
                projectDirPath + "/package_conflicts_ambiguous_definition").getPath();

        StringBuilder sb = new StringBuilder();
        compileProject(fileName, testDirPath, sb, false);

        externs.clear();

        String out = sb.toString();
        out = out.replace("\\", "/");

        assertThat(out, is(testAdapter.getUnitTestBaseDir().getPath() +
                "/flexjs/projects/package_conflicts_ambiguous_definition/mypackage/TestClass.as(29:20)\nAmbiguous reference to Event\n" +
                testAdapter.getUnitTestBaseDir().getPath() +
                "/flexjs/projects/package_conflicts_ambiguous_definition/mypackage/TestClass.as(30:41)\nAmbiguous reference to Event\n"));
    }

    @Test
    public void test_PackageConflict_SamePackageAsConflict()
    {
        String testDirPath = projectDirPath + "/package_conflicts_same_package_as_conflict";

        String fileName = "SamePackageAsConflict";

        externs.add("Event");

        sourcePath = new File(TestAdapterFactory.getTestAdapter().getUnitTestBaseDir(),
                projectDirPath + "/package_conflicts_same_package_as_conflict").getPath();

        List<String> compiledFileNames = compileProject(fileName, testDirPath);

        externs.clear();
        
        assertProjectOut(compiledFileNames, testDirPath);
    }

    @Test
    public void test_PackageConflict_DifferentPackageAsConflict()
    {
        String testDirPath = projectDirPath + "/package_conflicts_different_package_as_conflict";

        String fileName = "DifferentPackageAsConflict";

        externs.add("Event");

        sourcePath = new File(TestAdapterFactory.getTestAdapter().getUnitTestBaseDir(),
                projectDirPath + "/package_conflicts_different_package_as_conflict").getPath();

        List<String> compiledFileNames = compileProject(fileName, testDirPath);

        externs.clear();
        
        assertProjectOut(compiledFileNames, testDirPath);
    }

    @Test
    public void test_PackageConflict_UseWindow()
    {
        String testDirPath = projectDirPath + "/package_conflicts_use_window";

        String fileName = "UseWindow";

        externs.add("Event");

        sourcePath = new File(TestAdapterFactory.getTestAdapter().getUnitTestBaseDir(),
                projectDirPath + "/package_conflicts_use_window").getPath();

        List<String> compiledFileNames = compileProject(fileName, testDirPath);

        externs.clear();
        
        assertProjectOut(compiledFileNames, testDirPath);
    }

    @Test
    public void test_PackageConflict_NoConflictNoWindow()
    {
        String testDirPath = projectDirPath + "/package_conflicts_no_conflict_no_window";

        String fileName = "NoConflictNoWindow";

        externs.add("Event");

        sourcePath = new File(TestAdapterFactory.getTestAdapter().getUnitTestBaseDir(),
                projectDirPath + "/package_conflicts_no_conflict_no_window").getPath();

        List<String> compiledFileNames = compileProject(fileName, testDirPath);

        externs.clear();
        
        assertProjectOut(compiledFileNames, testDirPath);
    }

    @Test
    public void test_PackageConflict_NoConflictUseWindow()
    {
        String testDirPath = projectDirPath + "/package_conflicts_no_conflict_use_window";

        String fileName = "NoConflictUseWindow";

        externs.add("Event");

        sourcePath = new File(TestAdapterFactory.getTestAdapter().getUnitTestBaseDir(),
                projectDirPath + "/package_conflicts_no_conflict_use_window").getPath();

        List<String> compiledFileNames = compileProject(fileName, testDirPath);

        externs.clear();
        
        assertProjectOut(compiledFileNames, testDirPath);
    }

    @Override
    protected void addSourcePaths(List<File> sourcePaths)
    {
        sourcePaths.add(new File(FilenameNormalization.normalize(sourcePath)));
        ((FlexJSProject)project).unitTestExterns = externs;
    }

    @Override
    protected IBackend createBackend()
    {
        return new FlexJSBackend();
    }

}
