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

import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.codegen.js.goog.TestGoogAccessorMembers;
import org.apache.flex.compiler.internal.driver.js.flexjs.FlexJSBackend;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.junit.Test;

/**
 * @author Erik de Bruin
 */
public class TestFlexJSAccessorMembers extends TestGoogAccessorMembers
{
    @Override
    @Test
    public void testGetAccessor()
    {
        IClassNode node = (IClassNode) getNode("function get foo():int{}",
        		IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nFalconTest_A = function() {\n};\n\n\nObject.defineProperties(FalconTest_A.prototype, /** @lends {FalconTest_A.prototype} */ {\n/** @export */\nfoo: {\nget: /** @this {FalconTest_A} */ function() {\n}}}\n);");
    }

    @Override
    @Test
    public void testGetAccessor_withBody()
    {
    	IClassNode node = (IClassNode) getNode("function get foo():int{return -1;}",
    			IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nFalconTest_A = function() {\n};\n\n\nObject.defineProperties(FalconTest_A.prototype, /** @lends {FalconTest_A.prototype} */ {\n/** @export */\nfoo: {\nget: /** @this {FalconTest_A} */ function() {\n  return -1;\n}}}\n);");
    }

    @Override
    @Test
    public void testGetAccessor_withNamespace()
    {
    	IClassNode node = (IClassNode) getNode("public function get foo():int{return -1;}",
        		IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nFalconTest_A = function() {\n};\n\n\nObject.defineProperties(FalconTest_A.prototype, /** @lends {FalconTest_A.prototype} */ {\n/** @export */\nfoo: {\nget: /** @this {FalconTest_A} */ function() {\n  return -1;\n}}}\n);");
    }

    @Override
    @Test
    public void testGetAccessor_withNamespaceOverride()
    {
    	IClassNode node = (IClassNode) getNode("public class B extends A { public override function get foo():int{return super.foo;} }; public class A {public function get foo():int {return 0;}} ",
        		IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {A}\n */\nB = function() {\n  B.base(this, 'constructor');\n};\ngoog.inherits(B, A);\n\n\nObject.defineProperties(B.prototype, /** @lends {B.prototype} */ {\n/** @export */\nfoo: {\nget: /** @this {B} */ function() {\n  return org.apache.flex.utils.Language.superGetter(B, this, 'foo');\n}}}\n);");
    }

    @Test
    public void testGetAccessor_withGeneratedSetOverride()
    {
    	IClassNode node = (IClassNode) getNode("public class B extends A { public override function get foo():int{return super.foo;} }; public class A { public function set foo(value:int):void{} public function get foo():int {return 0;}}",
        		IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {A}\n */\nB = function() {\n  B.base(this, 'constructor');\n};\ngoog.inherits(B, A);\n\n\nObject.defineProperties(B.prototype, /** @lends {B.prototype} */ {\n/** @export */\nfoo: {\nget: /** @this {B} */ function() {\n  return org.apache.flex.utils.Language.superGetter(B, this, 'foo');\n},\nset: /** @this {B} */ function(value) {\norg.apache.flex.utils.Language.superSetter(B, this, 'foo', value);\n}}}\n);");
    }
    
    @Override
    @Test
    public void testGetAccessor_withStatic()
    {
    	IClassNode node = (IClassNode) getNode("public static function get foo():int{return -1;}",
        		IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nFalconTest_A = function() {\n};Object.defineProperties(FalconTest_A, /** @lends {FalconTest_A} */ {\n/** @expose */\nfoo: {\nget: function() {\n  return -1;\n}}}\n);");
    }

    @Override
    @Test
    public void testSetAccessor()
    {
    	IClassNode node = (IClassNode) getNode("function set foo(value:int):void{}",
        		IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nFalconTest_A = function() {\n};\n\n\nObject.defineProperties(FalconTest_A.prototype, /** @lends {FalconTest_A.prototype} */ {\n/** @export */\nfoo: {\nset: /** @this {FalconTest_A} */ function(value) {\n}}}\n);");
    }

    @Override
    @Test
    public void testSetAccessor_withBody()
    {
    	IClassNode node = (IClassNode) getNode("function set foo(value:int):void{fetch('haai');}",
        		IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nFalconTest_A = function() {\n};\n\n\nObject.defineProperties(FalconTest_A.prototype, /** @lends {FalconTest_A.prototype} */ {\n/** @export */\nfoo: {\nset: /** @this {FalconTest_A} */ function(value) {\n  fetch('haai');\n}}}\n);");
    }

    @Override
    @Test
    public void testSetAccessor_withNamespace()
    {
    	IClassNode node = (IClassNode) getNode("public function set foo(value:int):void{}",
        		IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nFalconTest_A = function() {\n};\n\n\nObject.defineProperties(FalconTest_A.prototype, /** @lends {FalconTest_A.prototype} */ {\n/** @export */\nfoo: {\nset: /** @this {FalconTest_A} */ function(value) {\n}}}\n);");
    }

    @Override
    @Test
    public void testSetAccessor_withNamespaceOverride()
    {
    	IClassNode node = (IClassNode) getNode("public class B extends A { public override function set foo(value:int):void {super.foo = value;} }; public class A { public function set foo(value:int):void{}}",
        		IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {A}\n */\nB = function() {\n  B.base(this, 'constructor');\n};\ngoog.inherits(B, A);\n\n\nObject.defineProperties(B.prototype, /** @lends {B.prototype} */ {\n/** @export */\nfoo: {\nset: /** @this {B} */ function(value) {\n  org.apache.flex.utils.Language.superSetter(B, this, 'foo', value);\n}}}\n);");
    }

    @Override
    @Test
    public void testSetAccessor_withStatic()
    {
    	IClassNode node = (IClassNode) getNode("public static function set foo(value:int):void{}",
        		IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nFalconTest_A = function() {\n};Object.defineProperties(FalconTest_A, /** @lends {FalconTest_A} */ {\n/** @expose */\nfoo: {\nset: function(value) {\n}}}\n);");
    }

    @Test
    public void testSetAccessor_withGeneratedGetOverride()
    {
    	IClassNode node = (IClassNode) getNode("public class B extends A { public override function set foo(value:int):void {super.foo = value;} }; public class A { public function set foo(value:int):void{} public function get foo():int { return 0;}}",
        		IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {A}\n */\nB = function() {\n  B.base(this, 'constructor');\n};\ngoog.inherits(B, A);\n\n\nObject.defineProperties(B.prototype, /** @lends {B.prototype} */ {\n/** @export */\nfoo: {\nget: /** @this {B} */ function() {\nreturn org.apache.flex.utils.Language.superGetter(B, this, 'foo');\n},\nset: /** @this {B} */ function(value) {\n  org.apache.flex.utils.Language.superSetter(B, this, 'foo', value);\n}}}\n);");
    }
    
    @Override
    protected IBackend createBackend()
    {
        return new FlexJSBackend();
    }
}
