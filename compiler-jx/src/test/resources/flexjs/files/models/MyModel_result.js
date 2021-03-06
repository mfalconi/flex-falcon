/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * models.MyModel
 *
 * @fileoverview
 *
 * @suppress {checkTypes|accessControls}
 */

goog.provide('models.MyModel');

goog.require('org.apache.flex.events.Event');
goog.require('org.apache.flex.events.EventDispatcher');



/**
 * @constructor
 * @extends {org.apache.flex.events.EventDispatcher}
 */
models.MyModel = function() {
  models.MyModel.base(this, 'constructor');

this._strings = ["AAPL", "ADBE", "GOOG", "MSFT", "YHOO"];
this._cities = ["London", "Miami", "Paris", "Sydney", "Tokyo"];
};
goog.inherits(models.MyModel, org.apache.flex.events.EventDispatcher);


/**
 * @private
 * @type {string}
 */
models.MyModel.prototype._labelText;


/**
 * @private
 * @type {Array}
 */
models.MyModel.prototype._strings;


/**
 * @private
 * @type {Array}
 */
models.MyModel.prototype._cities;


models.MyModel.prototype.get__labelText = function() {
  return this._labelText;
};


models.MyModel.prototype.set__labelText = function(value) {
  if (value != this._labelText) {
    this._labelText = value;
    this.dispatchEvent(new org.apache.flex.events.Event("labelTextChanged"));
  }
};


models.MyModel.prototype.get__strings = function() {
  return this._strings;
};


models.MyModel.prototype.get__cities = function() {
  return this._cities;
};


Object.defineProperties(models.MyModel.prototype, /** @lends {models.MyModel.prototype} */ {
/** @export */
labelText: {
get: models.MyModel.prototype.get__labelText,
set: models.MyModel.prototype.set__labelText},
/** @export */
strings: {
get: models.MyModel.prototype.get__strings},
/** @export */
cities: {
get: models.MyModel.prototype.get__cities}}
);


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
models.MyModel.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'MyModel', qName: 'models.MyModel', kind: 'class' }] };


/**
 * Prevent renaming of class. Needed for reflection.
 */
goog.exportSymbol('models.MyModel', models.MyModel);



/**
 * Reflection
 *
 * @return {Object.<string, Function>}
 */
models.MyModel.prototype.FLEXJS_REFLECTION_INFO = function () {
  return {
    variables: function () {return {};},
    accessors: function () {
      return {
        'labelText': { type: 'String', access: 'readwrite', declaredBy: 'models.MyModel'},
        'strings': { type: 'Array', access: 'readonly', declaredBy: 'models.MyModel'},
        'cities': { type: 'Array', access: 'readonly', declaredBy: 'models.MyModel'}
      };
    },
    methods: function () {
      return {
        'MyModel': { type: '', declaredBy: 'models.MyModel'}
      };
    }
  };
};
