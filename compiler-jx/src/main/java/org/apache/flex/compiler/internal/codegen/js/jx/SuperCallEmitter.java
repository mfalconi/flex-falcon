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

package org.apache.flex.compiler.internal.codegen.js.jx;

import org.apache.flex.compiler.clients.MXMLJSC;
import org.apache.flex.compiler.clients.MXMLJSC.JSOutputType;
import org.apache.flex.compiler.codegen.js.IJSEmitter;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.INamespaceDefinition;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSSessionModel;
import org.apache.flex.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSEmitter;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.utils.EmitterUtils;
import org.apache.flex.compiler.internal.definitions.FunctionDefinition;
import org.apache.flex.compiler.internal.tree.as.BinaryOperatorAssignmentNode;
import org.apache.flex.compiler.internal.tree.as.FunctionCallNode;
import org.apache.flex.compiler.internal.tree.as.IdentifierNode;
import org.apache.flex.compiler.internal.tree.as.MemberAccessExpressionNode;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IFunctionCallNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;

public class SuperCallEmitter extends JSSubEmitter
{

    public SuperCallEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    public void emit(IASNode node, String type)
    {
        IFunctionNode fnode = (node instanceof IFunctionNode) ? (IFunctionNode) node
                : null;
        IFunctionCallNode fcnode = (node instanceof IFunctionCallNode) ? (FunctionCallNode) node
                : null;

        final IClassDefinition thisClass = getModel().getCurrentClass();

        if (type == JSSessionModel.SUPER_FUNCTION_CALL)
        {
            if (fnode == null)
                fnode = (IFunctionNode) fcnode
                        .getAncestorOfType(IFunctionNode.class);

            if (fnode != null && fnode.isConstructor()
                    && !EmitterUtils.hasSuperClass(getProject(), fnode))
                return;

            IClassNode cnode = (IClassNode) node
                    .getAncestorOfType(IClassNode.class);

            if (fnode != null
                    && (fnode.getNodeID() == ASTNodeID.GetterID || fnode
                            .getNodeID() == ASTNodeID.SetterID))
            {
                if (cnode == null && thisClass != null)
                    write(getEmitter().formatQualifiedName(
                            thisClass.getQualifiedName()));
                else
                    write(getEmitter().formatQualifiedName(
                            cnode.getQualifiedName()));
                write(ASEmitterTokens.MEMBER_ACCESS);
                write(JSGoogEmitterTokens.SUPERCLASS);
                write(ASEmitterTokens.MEMBER_ACCESS);
                if (fnode.getNodeID() == ASTNodeID.GetterID)
                    write(JSFlexJSEmitterTokens.GETTER_PREFIX);
                else
                    write(JSFlexJSEmitterTokens.SETTER_PREFIX);
                write(fnode.getName());
                write(ASEmitterTokens.MEMBER_ACCESS);
                write(JSEmitterTokens.APPLY);
                write(ASEmitterTokens.PAREN_OPEN);
                write(ASEmitterTokens.THIS);

                IASNode[] anodes = null;
                boolean writeArguments = false;
                if (fcnode != null)
                {
                    anodes = fcnode.getArgumentNodes();

                    writeArguments = anodes.length > 0;
                }
                else if (fnode != null && fnode.isConstructor())
                {
                    anodes = fnode.getParameterNodes();

                    writeArguments = (anodes != null && anodes.length > 0);
                }
                else if (node instanceof IFunctionNode
                        && node instanceof BinaryOperatorAssignmentNode)
                {
                    BinaryOperatorAssignmentNode bnode = (BinaryOperatorAssignmentNode) node;

                    IFunctionNode pnode = (IFunctionNode) bnode
                            .getAncestorOfType(IFunctionNode.class);

                    if (pnode.getNodeID() == ASTNodeID.SetterID)
                    {
                        writeToken(ASEmitterTokens.COMMA);
                        writeToken(ASEmitterTokens.SQUARE_OPEN);
                        getWalker().walk(bnode.getRightOperandNode());
                        writeToken(ASEmitterTokens.SQUARE_CLOSE);
                    }
                }

                if (writeArguments)
                {
                	// I think len has to be 0 or 1
                    int len = anodes.length;
                    for (int i = 0; i < len; i++)
                    {
                        writeToken(ASEmitterTokens.COMMA);
                        writeToken(ASEmitterTokens.SQUARE_OPEN);

                        getWalker().walk(anodes[i]);
                        writeToken(ASEmitterTokens.SQUARE_CLOSE);
                    }
                }

                write(ASEmitterTokens.PAREN_CLOSE);
                return;
            }
        }
        super_emitSuperCall(node, type);
    }

    protected void super_emitSuperCall(IASNode node, String type)
    {
        // TODO (mschmalle) will remove this cast as more things get abstracted
        JSFlexJSEmitter fjs = (JSFlexJSEmitter) getEmitter();

        IFunctionNode fnode = (node instanceof IFunctionNode) ? (IFunctionNode) node
                : null;
        IFunctionCallNode fcnode = (node instanceof IFunctionCallNode) ? (FunctionCallNode) node
                : null;

        if (type == JSSessionModel.CONSTRUCTOR_EMPTY)
        {
            indentPush();
            writeNewline();
            indentPop();
        }
        else if (type == JSSessionModel.SUPER_FUNCTION_CALL)
        {
            if (fnode == null)
                fnode = (IFunctionNode) fcnode
                        .getAncestorOfType(IFunctionNode.class);
        }

        if (fnode.isConstructor()
                && !EmitterUtils.hasSuperClass(getProject(), fnode))
            return;

        IClassNode cnode = (IClassNode) node
                .getAncestorOfType(IClassNode.class);

        if (cnode == null)
        {
            IDefinition cdef = getModel().getCurrentClass();
            write(fjs.formatQualifiedName(cdef.getQualifiedName()));
        }
        else
            write(fjs.formatQualifiedName(cnode.getQualifiedName()));
        if (fnode.isConstructor())
        {
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(JSGoogEmitterTokens.GOOG_BASE);
            write(ASEmitterTokens.PAREN_OPEN);
            write(ASEmitterTokens.THIS);

            writeToken(ASEmitterTokens.COMMA);
            write(ASEmitterTokens.SINGLE_QUOTE);
            write(JSGoogEmitterTokens.GOOG_CONSTRUCTOR);
            write(ASEmitterTokens.SINGLE_QUOTE);
        }

        boolean usingApply = false;
        boolean isCustomNamespace = false;
        if (fnode != null && !fnode.isConstructor())
        {
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(JSGoogEmitterTokens.SUPERCLASS);
            IExpressionNode namenode = fcnode.getNameNode();
            IDefinition def = namenode.resolve(getWalker().getProject());
            String superName = fnode.getName();
            if (namenode instanceof MemberAccessExpressionNode)
            {
            	namenode = ((MemberAccessExpressionNode)namenode).getRightOperandNode();
            	if (namenode instanceof IdentifierNode)
            	{
            		superName = ((IdentifierNode)namenode).getName();
            	}
            }
            if (def instanceof FunctionDefinition && fjs.isCustomNamespace((FunctionDefinition)def))
            {
            	INamespaceDefinition nsDef = ((FunctionDefinition)def).getNamespaceReference().resolveNamespaceReference(getProject());
            	if (nsDef.getContainingScope() != null) // was null for flash_proxy in unit test
            		fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names 
    			String s = nsDef.getURI();
    			superName = s + "::" + superName;
    			isCustomNamespace = true;
            }
            if (isCustomNamespace)
            {
            	write(ASEmitterTokens.SQUARE_OPEN);
            	write(ASEmitterTokens.SINGLE_QUOTE);
            }
            else
                write(ASEmitterTokens.MEMBER_ACCESS);
            write(superName);
            if (isCustomNamespace)
            {
            	write(ASEmitterTokens.SINGLE_QUOTE);
                write(ASEmitterTokens.SQUARE_CLOSE);
            }
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(JSEmitterTokens.APPLY);
            write(ASEmitterTokens.PAREN_OPEN);
            write(ASEmitterTokens.THIS);
            usingApply = true;
        }

        IASNode[] anodes = null;
        boolean writeArguments = false;
        if (fcnode != null)
        {
            anodes = fcnode.getArgumentNodes();

            writeArguments = anodes.length > 0;
        }
        else if (fnode.isConstructor())
        {
        	// I think we only get here for implicit super calls
        	// (when there is no mention of super() in the constructor
        	// code and the compiler autogenerates the super() call)
        	// and implicit super calls do not have parameters passed
        	// to them.
        	/*
            anodes = fnode.getParameterNodes();

            writeArguments = (anodes != null && anodes.length > 0);
            */
        }

        if (writeArguments)
        {
        	if (usingApply)
        	{
                writeToken(ASEmitterTokens.COMMA);
                writeToken(ASEmitterTokens.SQUARE_OPEN);
        	}

            int len = anodes.length;
            for (int i = 0; i < len; i++)
            {
            	if (!usingApply || i > 0)
            		writeToken(ASEmitterTokens.COMMA);

                getWalker().walk(anodes[i]);
            }
        	if (usingApply)
                writeToken(ASEmitterTokens.SQUARE_CLOSE);
        }

        write(ASEmitterTokens.PAREN_CLOSE);

        if (type == JSSessionModel.CONSTRUCTOR_FULL)
        {
            write(ASEmitterTokens.SEMICOLON);
            writeNewline();
        }
        else if (type == JSSessionModel.CONSTRUCTOR_EMPTY)
        {
            write(ASEmitterTokens.SEMICOLON);
        }
    }
}
