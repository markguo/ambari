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
var stringUtils = require('utils/string_utils');

App.upgradeWizardView = Em.View.extend({
  controllerBinding: 'App.router.mainAdminStackAndUpgradeController',
  templateName: require('templates/main/admin/stack_upgrade/stack_upgrade_wizard'),

  /**
   * @type {Array}
   */
  failedStatuses: ['HOLDING_FAILED', 'HOLDING_TIMED_OUT', 'FAILED', 'TIMED_OUT'],

  /**
   * @type {Array}
   */
  activeStatuses: ['HOLDING_FAILED', 'HOLDING_TIMED_OUT', 'FAILED', 'TIMED_OUT', 'HOLDING', 'IN_PROGRESS'],

  /**
   * update timer
   * @type {number|null}
   * @default null
   */
  updateTimer: null,

  /**
   * @type {boolean}
   */
  isLoaded: false,

  /**
   * @type {boolean}
   */
  isDetailsOpened: false,

  /**
   * @type {boolean}
   */
  outsideView: true,

  /**
   * Downgrade should be available only if target version higher than current, so we can't downgrade
   * when downgrade already started
   * @type {boolean}
   */
  isDowngradeAvailable: function () {
    return stringUtils.compareVersions(this.get('controller.upgradeVersion'), this.get('controller.currentVersion.repository_version')) === 1;
  }.property('controller.currentVersion', 'controller.upgradeVersion'),

  /**
   * progress value is rounded to floor
   * @type {number}
   */
  overallProgress: function () {
    return Math.floor(this.get('controller.upgradeData.Upgrade.progress_percent'));
  }.property('controller.upgradeData.Upgrade.progress_percent'),

  /**
   * upgrade groups, reversed and PENDING ones are hidden
   * @type {Array}
   */
  upgradeGroups: function () {
    if (Em.isNone(this.get('controller.upgradeData.upgradeGroups'))) return [];
    var upgradeGroups = this.get('controller.upgradeData.upgradeGroups');
    upgradeGroups.forEach(function (group) {
      group.get('upgradeItems').reverse();
    });
    upgradeGroups.reverse();
    return upgradeGroups;
  }.property('controller.upgradeData.upgradeGroups'),

  /**
   * currently active group
   * @type {object|undefined}
   */
  activeGroup: function () {
    return this.get('upgradeGroups').find(function (item) {
      return this.get('activeStatuses').contains(item.get('status'));
    }, this);
  }.property('upgradeGroups.@each.status'),

  /**
   * if upgrade group is in progress it should have currently running item
   * @type {object|undefined}
   */
  runningItem: function () {
    return this.get('activeGroup.upgradeItems') && this.get('activeGroup.upgradeItems').findProperty('status', 'IN_PROGRESS');
  }.property('activeGroup.upgradeItems.@each.status'),

  /**
   * if upgrade group is failed it should have failed item
   * @type {object|undefined}
   */
  failedItem: function () {
    return this.get('activeGroup.upgradeItems') && this.get('activeGroup.upgradeItems').find(function (item) {
      return this.get('failedStatuses').contains(item.get('status'));
    }, this);
  }.property('activeGroup.upgradeItems.@each.status'),

  /**
   * details of currently active task
   * @type {object|undefined}
   */
  taskDetails: function () {
    if (this.get('runningItem')) {
      return this.get('runningItem').get('tasks').findProperty('status', 'IN_PROGRESS');
    } else if (this.get('failedItem')) {
      return this.get('failedItem').get('tasks').find(function (task) {
        return this.get('failedStatuses').contains(task.get('status'));
      }, this);
    }
  }.property('failedItem.tasks.@each.status', 'runningItem.tasks.@each.status'),

  /**
   * indicate whether failed item can be skipped or retried in order to continue Upgrade
   * @type {boolean}
   */
  isHoldingState: function () {
    return Boolean(this.get('failedItem.status') && this.get('failedItem.status').contains('HOLDING'));
  }.property('failedItem.status'),

  /**
   * indicate whether failed item can be skipped or retried in order to continue Upgrade
   * @type {boolean}
   */
  isSkipable: function () {
    return this.get('failedItem.skippable');
  }.property('failedItem.skippable'),

  /**
   * @type {boolean}
   */
  isManualDone: false,

  /**
   * @type {boolean}
   */
  isManualProceedDisabled: function () {
    return !this.get('isManualDone');
  }.property('isManualDone'),

  /**
   * if upgrade group is manual it should have manual item
   * @type {object|undefined}
   */
  manualItem: function () {
    return this.get('activeGroup.upgradeItems') && this.get('activeGroup.upgradeItems').findProperty('status', 'HOLDING');
  }.property('activeGroup.upgradeItems.@each.status'),

  /**
   * label of Upgrade status
   * @type {string}
   */
  upgradeStatusLabel: function() {
    switch (this.get('controller.upgradeData.Upgrade.request_status')) {
      case 'QUEUED':
      case 'PENDING':
      case 'IN_PROGRESS':
        return Em.I18n.t('admin.stackUpgrade.state.inProgress');
      case 'COMPLETED':
        return Em.I18n.t('admin.stackUpgrade.state.completed');
      case 'ABORTED':
      case 'TIMED_OUT':
      case 'FAILED':
      case 'HOLDING_FAILED':
      case 'HOLDING_TIMED_OUT':
      case 'HOLDING':
        return Em.I18n.t('admin.stackUpgrade.state.paused');
      default:
        return ""
    }
  }.property('controller.upgradeData.Upgrade.request_status'),

  /**
   * toggle details box
   */
  toggleDetails: function () {
    this.toggleProperty('isDetailsOpened');
  },

  /**
   * start polling upgrade data
   */
  startPolling: function () {
    var self = this;
    if (App.get('clusterName')) {
      this.get('controller').loadUpgradeData().done(function () {
        self.set('isLoaded', true);
      });
      this.doPolling();
    }
  }.observes('App.clusterName'),

  /**
   * start polling upgrade data
   */
  willInsertElement: function () {
    this.startPolling();
  },

  /**
   * stop polling upgrade data
   */
  willDestroyElement: function () {
    clearTimeout(this.get('updateTimer'));
    this.set('isLoaded', false);
  },

  /**
   * load upgrade data with time interval
   */
  doPolling: function () {
    var self = this;
    this.set('updateTimer', setTimeout(function () {
      self.get('controller').loadUpgradeData();
      self.doPolling();
    }, App.bgOperationsUpdateInterval));
  },

  /**
   * set status to Upgrade item
   * @param item
   * @param status
   */
  setUpgradeItemStatus: function(item, status) {
    return App.ajax.send({
      name: 'admin.upgrade.upgradeItem.setState',
      sender: this,
      data: {
        upgradeId: item.get('request_id'),
        itemId: item.get('stage_id'),
        groupId: item.get('group_id'),
        status: status
      }
    }).done(function () {
        item.set('status', status);
      });
  },

  /**
   * set current upgrade item state to FAILED (for HOLDING_FAILED) or TIMED_OUT (for HOLDING_TIMED_OUT)
   * in order to ignore fail and continue Upgrade
   * @param {object} event
   */
  continue: function (event) {
    this.setUpgradeItemStatus(event.context, event.context.get('status').slice(8));
  },

  /**
   * set current upgrade item state to PENDING in order to retry Upgrade
   * @param {object} event
   */
  retry: function (event) {
    this.setUpgradeItemStatus(event.context, 'PENDING');
  },

  /**
   * set current upgrade item state to COMPLETED in order to proceed
   * @param {object} event
   */
  complete: function (event) {
    this.setUpgradeItemStatus(event.context, 'COMPLETED');
  },

  /**
   * set current upgrade item state to FAILED in order to cancel upgrade
   * @param {object} event
   */
  cancel: function (event) {
    this.setUpgradeItemStatus(event.context, 'FAILED');
  }
});
