"""
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

"""

from resource_management.libraries.functions.version import format_hdp_stack_version, compare_versions
from resource_management.libraries.functions.default import default
from resource_management import *

config = Script.get_config()

stack_name = default("/hostLevelParams/stack_name", None)

user_group = config['configurations']['cluster-env']['user_group']
proxyuser_group =  config['configurations']['hadoop-env']['proxyuser_group']

security_enabled = False

stack_version_unformatted = str(config['hostLevelParams']['stack_version'])
hdp_stack_version = format_hdp_stack_version(stack_version_unformatted)

#hadoop params
if hdp_stack_version != "" and compare_versions(hdp_stack_version, '2.2') >= 0:
  flume_bin = '/usr/hdp/current/flume-server/bin/flume-ng'
  flume_hive_home = '/usr/hdp/current/hive-metastore'
  flume_hcat_home = '/usr/hdp/current/hive-webhcat'
else:
  flume_bin = '/usr/bin/flume-ng'
  flume_hive_home = '/usr/lib/hive'
  flume_hcat_home = '/usr/lib/hive-hcatalog'

flume_conf_dir = '/etc/flume/conf'
java_home = config['hostLevelParams']['java_home']
flume_log_dir = '/var/log/flume'
flume_run_dir = '/var/run/flume'
flume_user = 'flume'
flume_group = 'flume'

if 'flume-env' in config['configurations'] and 'flume_user' in config['configurations']['flume-env']:
  flume_user = config['configurations']['flume-env']['flume_user']

if (('flume-conf' in config['configurations']) and('content' in config['configurations']['flume-conf'])):
  flume_conf_content = config['configurations']['flume-conf']['content']
else:
  flume_conf_content = None

if (('flume-log4j' in config['configurations']) and ('content' in config['configurations']['flume-log4j'])):
  flume_log4j_content = config['configurations']['flume-log4j']['content']
else:
  flume_log4j_content = None

targets = default('/commandParams/flume_handler', None)
flume_command_targets = [] if targets is None else targets.split(',')

flume_env_sh_template = config['configurations']['flume-env']['content']

ganglia_server_hosts = default('/clusterHostInfo/ganglia_server_host', [])
ganglia_server_host = None
if 0 != len(ganglia_server_hosts):
  ganglia_server_host = ganglia_server_hosts[0]

hostname = None
if config.has_key('hostname'):
  hostname = config['hostname']

ams_collector_hosts = default("/clusterHostInfo/metric_collector_hosts", [])
has_metric_collector = not len(ams_collector_hosts) == 0
if has_metric_collector:
  metric_collector_host = ams_collector_hosts[0]
