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

package org.apache.flex.compiler.internal.js.codegen.goog;

import org.apache.flex.compiler.common.ASModifier;
import org.apache.flex.compiler.common.DependencyType;
import org.apache.flex.compiler.constants.IASGlobalFunctionConstants;
import org.apache.flex.compiler.constants.IASKeywordConstants;
import org.apache.flex.compiler.constants.IASLanguageConstants;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.definitions.references.IReference;
import org.apache.flex.compiler.internal.as.codegen.ASEmitter;
import org.apache.flex.compiler.internal.js.codegen.JSDocEmitter;
import org.apache.flex.compiler.internal.js.codegen.JSSharedData;
import org.apache.flex.compiler.internal.scopes.ASScope;
import org.apache.flex.compiler.internal.semantics.SemanticUtils;
import org.apache.flex.compiler.js.codegen.IJSEmitter;
import org.apache.flex.compiler.js.codegen.goog.IJSGoogDocEmitter;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IInterfaceNode;
import org.apache.flex.compiler.tree.as.IPackageNode;
import org.apache.flex.compiler.tree.as.IParameterNode;
import org.apache.flex.compiler.tree.as.IScopedNode;
import org.apache.flex.compiler.tree.as.IVariableNode;
import org.apache.flex.compiler.tree.mxml.IMXMLDocumentNode;

public class JSGoogDocEmitter extends JSDocEmitter implements IJSGoogDocEmitter
{
    public static final String AT = "@";
    public static final String PARAM = "param";
    public static final String STAR = "*";
    public static final String TYPE = "type";

    public JSGoogDocEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emitInterfaceDoc(IInterfaceNode node)
    {
        begin();

        emitJSDocLine(JSGoogEmitter.INTERFACE);

        String[] inodes = node.getExtendedInterfaces();
        for (String inode : inodes)
        {
            emitJSDocLine(IASKeywordConstants.EXTENDS, inode);
        }

        end();
    }

    @Override
    public void emitFieldDoc(IVariableNode node, IDefinition def)
    {
        begin();

        String ns = node.getNamespace();
        if (ns == IASKeywordConstants.PRIVATE)
        {
            emitPrivate(node);
        }
        else if (ns == IASKeywordConstants.PROTECTED)
        {
            emitProtected(node);
        }

        if (node.isConst())
            emitConst(node);

        String packageName = "";
        if (def != null)
            packageName = def.getPackageName();

        emitType(node, packageName);

        end();
    }

    @Override
    public void emitMethodDoc(IFunctionNode node, ICompilerProject project)
    {
        IClassDefinition classDefinition = resolveClassDefinition(node);

        if (node instanceof IFunctionNode)
        {
            boolean hasDoc = false;

            if (node.isConstructor())
            {
                // TODO (erikdebruin) handle JSDOC for constructors with arguments

                begin();
                hasDoc = true;

                emitJSDocLine(JSGoogEmitter.CONSTRUCTOR);

                IClassDefinition parent = (IClassDefinition) node
                        .getDefinition().getParent();
                IClassDefinition superClass = parent.resolveBaseClass(project);
                String qname = superClass.getQualifiedName();

                if (superClass != null
                        && !qname.equals(IASLanguageConstants.Object))
                    emitExtends(superClass, superClass.getPackageName());

                IReference[] references = classDefinition
                        .getImplementedInterfaceReferences();
                for (IReference iReference : references)
                {
                    ITypeDefinition type = (ITypeDefinition) iReference
                            .resolve(project, (ASScope) classDefinition
                                    .getContainingScope(),
                                    DependencyType.INHERITANCE, true);
                    emitImplements(type, type.getPackageName());
                }

                //                IExpressionNode[] inodes = cnode.getImplementedInterfaceNodes();
                //                if (inodes.length > 0)
                //                {
                //                    for (IExpressionNode inode : inodes)
                //                    {
                //                        emitImplements(inode.resolveType(project));
                //                    }
                //                }
            }
            else
            {
                // @this
                if (containsThisReference(node))
                {
                    begin();
                    hasDoc = true;

                    emitThis(classDefinition, classDefinition.getPackageName());
                }

                // @param
                IParameterNode[] parameters = node.getParameterNodes();
                for (IParameterNode pnode : parameters)
                {
                    if (!hasDoc)
                    {
                        begin();
                        hasDoc = true;
                    }

                    IExpressionNode enode = pnode.getNameExpressionNode();
                    emitParam(pnode, enode.resolveType(project)
                            .getPackageName());
                }

                // @return
                String returnType = node.getReturnType();
                if (returnType != ""
                        && returnType != ASTNodeID.LiteralVoidID
                                .getParaphrase())
                {
                    if (!hasDoc)
                    {
                        begin();
                        hasDoc = true;
                    }

                    emitReturn(node, node.getPackageName());
                }

                // @override
                Boolean override = node.hasModifier(ASModifier.OVERRIDE);
                if (override)
                {
                    if (!hasDoc)
                    {
                        begin();
                        hasDoc = true;
                    }

                    emitOverride(node);
                }
            }

            if (hasDoc)
                end();
        }
    }

    @Override
    public void emitVarDoc(IVariableNode node, IDefinition def)
    {
        String packageName = "";
        if (def != null)
            packageName = def.getPackageName();

        if (!node.isConst())
        {
            emitTypeShort(node, packageName);
        }
        else
        {
            writeNewline();
            begin();
            emitConst(node);
            emitType(node, packageName);
            end();
        }
    }

    @Override
    public void emitConst(IVariableNode node)
    {
        emitJSDocLine(IASKeywordConstants.CONST);
    }

    @Override
    public void emitExtends(IClassDefinition superDefinition, String packageName)
    {
        emitJSDocLine(IASKeywordConstants.EXTENDS,
                superDefinition.getQualifiedName());
    }

    @Override
    public void emitImplements(ITypeDefinition definition, String packageName)
    {
        emitJSDocLine(IASKeywordConstants.IMPLEMENTS,
                definition.getQualifiedName());
    }

    @Override
    public void emitOverride(IFunctionNode node)
    {
        emitJSDocLine(IASKeywordConstants.OVERRIDE);
    }

    @Override
    public void emitParam(IParameterNode node, String packageName)
    {
        String postfix = (node.getDefaultValue() == null) ? ""
                : ASEmitter.EQUALS;

        String paramType = "";
        if (node.isRest())
            paramType = IASLanguageConstants.REST;
        else
            paramType = convertASTypeToJS(node.getVariableType(), packageName);

        emitJSDocLine(PARAM, paramType + postfix, node.getName());
    }

    @Override
    public void emitPrivate(IASNode node)
    {
        emitJSDocLine(IASKeywordConstants.PRIVATE);
    }

    @Override
    public void emitProtected(IASNode node)
    {
        emitJSDocLine(IASKeywordConstants.PROTECTED);
    }

    @Override
    public void emitReturn(IFunctionNode node, String packageName)
    {
        String rtype = node.getReturnType();
        if (rtype != null)
        {
            emitJSDocLine(IASKeywordConstants.RETURN,
                    convertASTypeToJS(rtype, packageName));
        }
    }

    @Override
    public void emitThis(ITypeDefinition type, String packageName)
    {
        emitJSDocLine(IASKeywordConstants.THIS, type.getQualifiedName());
    }

    @Override
    public void emitType(IASNode node, String packageName)
    {
        String type = ((IVariableNode) node).getVariableType();
        emitJSDocLine(TYPE, convertASTypeToJS(type, packageName));
    }

    public void emitTypeShort(IASNode node, String packageName)
    {
        String type = ((IVariableNode) node).getVariableType();
        write(JSDOC_OPEN);
        writeSpace();
        write(AT);
        write(TYPE);
        writeSpace();
        writeBlockOpen();
        write(convertASTypeToJS(type, packageName));
        writeBlockClose();
        writeSpace();
        write(JSDOC_CLOSE);
        writeSpace();
    }

    //--------------------------------------------------------------------------

    public void emmitPackageHeader(IPackageNode node)
    {
        begin();
        write(ASEmitter.SPACE);
        write(STAR);
        write(ASEmitter.SPACE);
        write(JSSharedData.getTimeStampString());
        end();
    }

    //--------------------------------------------------------------------------

    private void emitJSDocLine(String name)
    {
        emitJSDocLine(name, "");
    }

    private void emitJSDocLine(String name, String type)
    {
        emitJSDocLine(name, type, "");
    }

    private void emitJSDocLine(String name, String type, String param)
    {
        writeSpace();
        write(STAR);
        writeSpace();
        write(AT);
        write(name);
        if (type != "")
        {
            writeSpace();
            writeBlockOpen();
            write(type);
            writeBlockClose();
        }
        if (param != "")
        {
            writeSpace();
            write(param);
        }
        writeNewline();
    }

    private boolean containsThisReference(IASNode node)
    {
        final int len = node.getChildCount();
        for (int i = 0; i < len; i++)
        {
            final IASNode child = node.getChild(i);
            if (child.getChildCount() > 0)
            {
                return containsThisReference(child);
            }
            else
            {
                if (SemanticUtils.isThisKeyword(child))
                    return true;
            }
        }

        return false;
    }

    private String convertASTypeToJS(String name, String pname)
    {
        String result = "";

        if (name.equals(""))
            result = IASLanguageConstants.ANY_TYPE;
        else if (name.equals(IASLanguageConstants.Boolean)
                || name.equals(IASLanguageConstants.String)
                || name.equals(IASLanguageConstants.Number))
            result = name.toLowerCase();
        else if (name.equals(IASLanguageConstants._int)
                || name.equals(IASLanguageConstants.uint))
            result = IASLanguageConstants.Number.toLowerCase();

        boolean isBuiltinFunction = name.matches("Vector\\.<.*>");
        IASGlobalFunctionConstants.BuiltinType[] builtinTypes = IASGlobalFunctionConstants.BuiltinType
                .values();
        for (IASGlobalFunctionConstants.BuiltinType builtinType : builtinTypes)
        {
            if (name.equalsIgnoreCase(builtinType.getName()))
            {
                isBuiltinFunction = true;
                break;
            }
        }

        if (result == "")
            result = (pname != "" && !isBuiltinFunction) ? pname
                    + ASEmitter.PERIOD + name : name;

        result = result.replace(IASLanguageConstants.String,
                IASLanguageConstants.String.toLowerCase());

        return result;
    }

    private IClassDefinition resolveClassDefinition(IFunctionNode node)
    {
        IScopedNode scope = node.getContainingScope();
        if (scope instanceof IMXMLDocumentNode)
            return ((IMXMLDocumentNode) scope).getClassDefinition();

        IClassNode cnode = (IClassNode) node
                .getAncestorOfType(IClassNode.class);
        return cnode.getDefinition();
    }
}