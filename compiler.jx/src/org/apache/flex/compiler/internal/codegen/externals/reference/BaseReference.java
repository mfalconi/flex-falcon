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

package org.apache.flex.compiler.internal.codegen.externals.reference;

import java.io.File;
import java.util.Set;

import org.apache.flex.compiler.clients.ExternCConfiguration.ExcludedMemeber;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfo.Marker;
import com.google.javascript.rhino.JSDocInfo.StringPosition;
import com.google.javascript.rhino.JSDocInfo.TypePosition;
import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.Node;

public abstract class BaseReference
{
    private String qualfiedName;

    protected JSDocInfo comment;

    private File currentFile;

    private Node node;

    private ReferenceModel model;

    public File getCurrentFile()
    {
        return currentFile;
    }

    public void setCurrentFile(File currentFile)
    {
        this.currentFile = currentFile;
    }

    public String getBaseName()
    {
        return qualfiedName.substring(qualfiedName.lastIndexOf('.') + 1);
    }

    public String getPackageName()
    {
        int end = qualfiedName.lastIndexOf('.');
        if (end == -1)
            return "";
        return qualfiedName.substring(0, end);
    }

    public String getQualifiedName()
    {
        return qualfiedName;
    }

    public final boolean isQualifiedName()
    {
        return qualfiedName.indexOf('.') != -1;
    }

    public Node getNode()
    {
        return node;
    }

    public void setNode(Node node)
    {
        this.node = node;
    }

    public void setComment(JSDocInfo comment)
    {
        this.comment = comment;
    }

    public JSDocInfo getComment()
    {
        return comment;
    }

    public Compiler getCompiler()
    {
        return model.getJSCompiler();
    }

    public ReferenceModel getModel()
    {
        return model;
    }

    public BaseReference(ReferenceModel model, Node node, String qualfiedName,
            JSDocInfo comment)
    {
        this.model = model;
        this.node = node;
        this.qualfiedName = qualfiedName;
        this.comment = comment;
    }

    public void emitComment(StringBuilder sb)
    {
        sb.append("    /**\n");
        emitCommentBody(sb);
        sb.append("     */\n");
    }

    protected void emitCommentBody(StringBuilder sb)
    {
        emitBlockDescription(sb);
        emitSee(sb);
        emitSeeSourceFileName(sb);
    }

    protected void emitBlockDescription(StringBuilder sb)
    {
        String blockDescription = getComment().getBlockDescription();
        if (blockDescription != null)
        {
            sb.append("     * ");
            sb.append(blockDescription.replaceAll("\\n", "\n     * "));
            sb.append("\n     *\n");
        }

    }

    protected void emitSee(StringBuilder sb)
    {
        for (Marker marker : getComment().getMarkers())
        {
            StringPosition name = marker.getAnnotation();
            TypePosition typePosition = marker.getType();
            StringPosition descriptionPosition = marker.getDescription();
            StringBuilder desc = new StringBuilder();

            // XXX Figure out how to toString() a TypePosition Node for markers
            // XXX Figure out how to get a @param name form the Marker
            if (!name.getItem().equals("see"))
                continue;

            if (name != null)
            {
                desc.append(name.getItem());
                desc.append(" ");
            }

            if (typePosition != null)
            {
                //desc.append(typePosition.getItem().getString());
                //desc.append(" ");
            }

            if (descriptionPosition != null)
            {
                desc.append(descriptionPosition.getItem());
                desc.append(" ");
            }

            sb.append("     * @" + desc.toString() + "\n");
        }
    }

    protected void emitSeeSourceFileName(StringBuilder sb)
    {
        sb.append("     * @see " + getNode().getSourceFileName() + "\n");
    }

    public ExcludedMemeber isExcluded()
    {
        return null;
    }

    public abstract void emit(StringBuilder sb);

    protected void emitFunctionCommentBody(StringBuilder sb)
    {
        emitBlockDescription(sb);
        emitParams(sb);
        emitSee(sb);
        emitSeeSourceFileName(sb);
        emitReturns(sb);
    }

    protected void emitParams(StringBuilder sb)
    {
        Set<String> parameterNames = getComment().getParameterNames();
        for (String paramName : parameterNames)
        {
            JSTypeExpression parameterType = getComment().getParameterType(
                    paramName);
            String description = getComment().getDescriptionForParameter(
                    paramName);
            sb.append("     * @param ");

            sb.append(paramName);
            sb.append(" ");

            if (parameterType != null)
            {
                sb.append("[");
                sb.append(parameterType.evaluate(null,
                        getModel().getJSCompiler().getTypeRegistry()).toAnnotationString());
                sb.append("]");
                sb.append(" ");
            }
            if (description != null)
                sb.append(description);
            sb.append("\n");
        }
    }

    protected void emitReturns(StringBuilder sb)
    {
        if (getComment().hasReturnType())
        {
            JSTypeExpression returnType = getComment().getReturnType();
            if (returnType != null)
            {
                sb.append("     * @returns ");
                sb.append("{");
                sb.append(returnType.evaluate(null,
                        getModel().getJSCompiler().getTypeRegistry()).toAnnotationString());
                sb.append("} ");
                String description = getComment().getReturnDescription();
                if (description != null)
                    sb.append(description);
                sb.append("\n");
            }
        }
    }
}
