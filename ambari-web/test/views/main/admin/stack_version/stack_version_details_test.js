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
require('views/main/admin/stack_versions/stack_version_view');
var view;

describe('App.MainStackVersionsDetailsView', function () {

  var defaultRepoVersion = "2.2.1.0";
  beforeEach(function () {
    view = App.MainStackVersionsDetailsView.create({
      "controller": {
        "hostsToInstall" : 0,
        "progress": 0,
        "doPolling": Em.K
      },
      "content": {
        "stackVersion":
          {
            "state" : "ANY"
          },
        'repositoryVersion': defaultRepoVersion
      },
      "currentHosts": ['currentHost'],
      "installedHosts": ['installedHost'],
      "notInstalledHosts": ['notInstalledHost']
    });
  });

  describe('#installButtonMsg', function () {
    it("install button msg for init state" , function() {
      view.set("controller.hostsToInstall", 2);
      view.set("content.stackVersion.state", "ANY");
      expect(view.get('installButtonMsg')).to.equal(Em.I18n.t('admin.stackVersions.details.hosts.btn.install').format(2))
    });

    it("install button msg for install failed state" , function() {
      view.set("content.stackVersion.state", "INSTALL_FAILED");
      expect(view.get('installButtonMsg')).to.equal(Em.I18n.t('admin.stackVersions.details.hosts.btn.reinstall'))
    });

    it("install button msg for out of sync failed state" , function() {
      view.set("controller.hostsToInstall", 1);
      view.set("content.stackVersion.state", "OUT_OF_SYNC");
      expect(view.get('installButtonMsg')).to.equal(Em.I18n.t('admin.stackVersions.details.hosts.btn.install').format(1))
    });
  });

  describe('#installButtonClass', function () {
    it("install button class for init state" , function() {
      view.set("content.stackVersion.state", "ANY");
      expect(view.get('installButtonClass')).to.equal('btn-success')
    });

    it("install button class install failed state" , function() {
      view.set("content.stackVersion.state", "INSTALL_FAILED");
      expect(view.get('installButtonClass')).to.equal('btn-danger')
    });

    it("install button class install out of sync state" , function() {
      view.set("content.stackVersion.state", "OUT_OF_SYNC");
      expect(view.get('installButtonClass')).to.equal('btn-success')
    });
  });

  describe('#progress', function () {
    it("this that is used as width of progress bar" , function() {
      view.set("controller.progress", 20);
      expect(view.get('progress')).to.equal('width:20%');
    });
  });

  describe('#didInsertElement', function () {
    beforeEach(function() {
      sinon.stub(App.get('router.mainStackVersionsController'), 'set', Em.K);
      sinon.stub(App.get('router.mainStackVersionsController'), 'load', Em.K);
      sinon.stub(App.get('router.mainStackVersionsController'), 'doPolling', Em.K);
      sinon.stub(view.get('controller'), 'doPolling', Em.K);
      sinon.stub(App.RepositoryVersion, 'find', function() {
        return [{id: 1}]
      });
    });
    afterEach(function() {
      App.get('router.mainStackVersionsController').set.restore();
      App.get('router.mainStackVersionsController').load.restore();
      App.get('router.mainStackVersionsController').doPolling.restore();
      view.get('controller').doPolling.restore();
      App.RepositoryVersion.find.restore();
    });
    it("runs polling and load when view is in dom" , function() {
      view.set('content.id', 2);
      view.didInsertElement();
      expect(App.get('router.mainStackVersionsController').set.calledWith('isPolling', true)).to.be.true;
      expect(App.get('router.mainStackVersionsController').load.calledOnce).to.be.true;
      expect(App.get('router.mainStackVersionsController').doPolling.calledOnce).to.be.true;
      expect(view.get('controller').doPolling.calledOnce).to.be.true;
    });

    it("runs polling when view is in dom" , function() {
      view.set('content.id', 1);
      view.didInsertElement();
      expect(App.get('router.mainStackVersionsController').set.calledWith('isPolling', true)).to.be.true;
      expect(App.get('router.mainStackVersionsController').load.calledOnce).to.be.false;
      expect(App.get('router.mainStackVersionsController').doPolling.calledOnce).to.be.true;
      expect(view.get('controller').doPolling.calledOnce).to.be.true;
    });
  });

  describe('#willDestroyElement', function () {
    beforeEach(function() {
      sinon.stub(App.get('router.mainStackVersionsController'), 'set', Em.K);
    });
    afterEach(function() {
      App.get('router.mainStackVersionsController').set.restore();
    });
    it("runs polling when view is in dom" , function() {
      view.willDestroyElement();
      expect(App.get('router.mainStackVersionsController').set.calledWith('isPolling', false)).to.be.true;
    });
  });

  describe('#showNotInstalledHosts', function() {
    beforeEach(function() {
      sinon.stub(App.get('router.repoVersionsManagementController'), 'showHostsListPopup', Em.K);
    });
    afterEach(function () {
      App.get('router.repoVersionsManagementController').showHostsListPopup.restore();
    });

    it("runs showHostsListPopup from repoVersionsManagementController", function () {
      var state = {
        'id': 'installing',
        'label': Em.I18n.t('admin.stackVersions.hosts.popup.header.not_installed')
      };
      view.showNotInstalledHosts();
      expect(App.get('router.repoVersionsManagementController').showHostsListPopup.calledWith(state, defaultRepoVersion, ['notInstalledHost'])).to.be.true;
    });
  });

  describe('#showInstalledHosts', function() {
    beforeEach(function() {
      sinon.stub(App.get('router.repoVersionsManagementController'), 'showHostsListPopup', Em.K);
    });
    afterEach(function () {
      App.get('router.repoVersionsManagementController').showHostsListPopup.restore();
    });

    it("runs showHostsListPopup from repoVersionsManagementController", function () {
      var state = {
        'id': 'installed',
        'label': Em.I18n.t('admin.stackVersions.hosts.popup.header.installed')
      };
      view.showInstalledHosts();
      expect(App.get('router.repoVersionsManagementController').showHostsListPopup.calledWith(state, defaultRepoVersion, ['installedHost'])).to.be.true;
    });
  });

  describe('#showCurrentHosts', function() {
    beforeEach(function() {
      sinon.stub(App.get('router.repoVersionsManagementController'), 'showHostsListPopup', Em.K);
    });
    afterEach(function () {
      App.get('router.repoVersionsManagementController').showHostsListPopup.restore();
    });

    it("runs showHostsListPopup from repoVersionsManagementController", function () {
      var state = {
        'id': 'current',
        'label': Em.I18n.t('admin.stackVersions.hosts.popup.header.current')
      };
      view.showCurrentHosts();
      expect(App.get('router.repoVersionsManagementController').showHostsListPopup.calledWith(state, defaultRepoVersion, ['currentHost'])).to.be.true;
    });
  });
});
