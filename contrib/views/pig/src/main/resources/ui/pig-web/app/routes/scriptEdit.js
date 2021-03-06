/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var App = require('app');

App.ScriptEditRoute = Em.Route.extend({
  enter:function () {
    this.controllerFor('script').set('activeTab','script');
  },
  isExec:false,
  model: function(params) {
    var record;
    var isExist = this.store.all('script').some(function(script) {
      return script.get('id') === params.script_id;
    });
    if (isExist) {
      record = this.store.find('script',params.script_id);
    } else {
      record = this.store.createRecord('script');
    }
    return record;
  },
  afterModel:function  (model) {
    if (model.get('length') == 0) {
      this.transitionTo('pig');
    }
    this.controllerFor('pig').set('activeScriptId', model.get('id'));
  },
  renderTemplate: function() {
    this.render('script/edit');
  }
});
