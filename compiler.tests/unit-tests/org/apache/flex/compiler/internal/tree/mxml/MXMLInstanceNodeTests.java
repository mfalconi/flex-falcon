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

package org.apache.flex.compiler.internal.tree.mxml;

import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;

/**
 * JUnit tests for {@link MXMLInstanceNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLInstanceNodeTests extends MXMLClassReferenceNodeTests
{
	protected String getPrefix()
	{
		return "<d:Sprite xmlns:fx='http://ns.adobe.com/mxml/2009' xmlns:d='flash.display.*' xmlns:s='library://ns.adobe.com/flex/spark' xmlns:mx='library://ns.adobe.com/flex/mx'>\n" +
	           "    <fx:Declarations>\n" +
		       "        ";
	}
			
    protected String getPostfix()
    {
    	return "\n" +
		       "    </fx:Declarations>\n" +
		       "</d:Sprite>";
    }
    
    @Override
    protected IMXMLFileNode getMXMLFileNode(String code)
    {
    	return super.getMXMLFileNode(getPrefix() + code + getPostfix());
    }
}
