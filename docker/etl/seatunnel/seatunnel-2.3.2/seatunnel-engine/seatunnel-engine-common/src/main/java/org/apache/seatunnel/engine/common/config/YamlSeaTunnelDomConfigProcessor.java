/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.seatunnel.engine.common.config;

import org.apache.seatunnel.engine.common.config.server.CheckpointConfig;
import org.apache.seatunnel.engine.common.config.server.CheckpointStorageConfig;
import org.apache.seatunnel.engine.common.config.server.QueueType;
import org.apache.seatunnel.engine.common.config.server.ServerConfigOptions;
import org.apache.seatunnel.engine.common.config.server.SlotServiceConfig;
import org.apache.seatunnel.engine.common.config.server.ThreadShareMode;

import org.w3c.dom.Node;

import com.hazelcast.config.InvalidConfigurationException;
import com.hazelcast.internal.config.AbstractDomConfigProcessor;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.hazelcast.internal.config.DomConfigHelper.childElements;
import static com.hazelcast.internal.config.DomConfigHelper.cleanNodeName;
import static com.hazelcast.internal.config.DomConfigHelper.getBooleanValue;
import static com.hazelcast.internal.config.DomConfigHelper.getIntegerValue;

public class YamlSeaTunnelDomConfigProcessor extends AbstractDomConfigProcessor {
    private static final ILogger LOGGER = Logger.getLogger(YamlSeaTunnelDomConfigProcessor.class);

    private final SeaTunnelConfig config;

    YamlSeaTunnelDomConfigProcessor(boolean domLevel3, SeaTunnelConfig config) {
        super(domLevel3);
        this.config = config;
    }

    @Override
    public void buildConfig(Node rootNode) {
        for (Node node : childElements(rootNode)) {
            String nodeName = cleanNodeName(node);
            if (occurrenceSet.contains(nodeName)) {
                throw new InvalidConfigurationException(
                        "Duplicate '" + nodeName + "' definition found in the configuration.");
            }
            if (handleNode(node, nodeName)) {
                continue;
            }
            if (!SeaTunnelConfigSections.canOccurMultipleTimes(nodeName)) {
                occurrenceSet.add(nodeName);
            }
        }
    }

    private boolean handleNode(Node node, String name) {
        if (SeaTunnelConfigSections.ENGINE.isEqual(name)) {
            parseEngineConfig(node, config);
        } else {
            return true;
        }
        return false;
    }

    private SlotServiceConfig parseSlotServiceConfig(Node slotServiceNode) {
        SlotServiceConfig slotServiceConfig = new SlotServiceConfig();
        for (Node node : childElements(slotServiceNode)) {
            String name = cleanNodeName(node);
            if (ServerConfigOptions.DYNAMIC_SLOT.key().equals(name)) {
                slotServiceConfig.setDynamicSlot(getBooleanValue(getTextContent(node)));
            } else if (ServerConfigOptions.SLOT_NUM.key().equals(name)) {
                slotServiceConfig.setSlotNum(
                        getIntegerValue(ServerConfigOptions.SLOT_NUM.key(), getTextContent(node)));
            } else {
                LOGGER.warning("Unrecognized element: " + name);
            }
        }
        return slotServiceConfig;
    }

    private void parseEngineConfig(Node engineNode, SeaTunnelConfig config) {
        final EngineConfig engineConfig = config.getEngineConfig();
        for (Node node : childElements(engineNode)) {
            String name = cleanNodeName(node);
            if (ServerConfigOptions.BACKUP_COUNT.key().equals(name)) {
                engineConfig.setBackupCount(
                        getIntegerValue(
                                ServerConfigOptions.BACKUP_COUNT.key(), getTextContent(node)));
            } else if (ServerConfigOptions.QUEUE_TYPE.key().equals(name)) {
                engineConfig.setQueueType(
                        QueueType.valueOf(getTextContent(node).toUpperCase(Locale.ROOT)));
            } else if (ServerConfigOptions.PRINT_EXECUTION_INFO_INTERVAL.key().equals(name)) {
                engineConfig.setPrintExecutionInfoInterval(
                        getIntegerValue(
                                ServerConfigOptions.PRINT_EXECUTION_INFO_INTERVAL.key(),
                                getTextContent(node)));
            } else if (ServerConfigOptions.PRINT_JOB_METRICS_INFO_INTERVAL.key().equals(name)) {
                engineConfig.setPrintJobMetricsInfoInterval(
                        getIntegerValue(
                                ServerConfigOptions.PRINT_JOB_METRICS_INFO_INTERVAL.key(),
                                getTextContent(node)));
            } else if (ServerConfigOptions.JOB_METRICS_BACKUP_INTERVAL.key().equals(name)) {
                engineConfig.setJobMetricsBackupInterval(
                        getIntegerValue(
                                ServerConfigOptions.JOB_METRICS_BACKUP_INTERVAL.key(),
                                getTextContent(node)));
            } else if (ServerConfigOptions.TASK_EXECUTION_THREAD_SHARE_MODE.key().equals(name)) {
                String mode = getTextContent(node).toUpperCase(Locale.ROOT);
                if (!Arrays.asList("ALL", "OFF", "PART").contains(mode)) {
                    throw new IllegalArgumentException(
                            ServerConfigOptions.TASK_EXECUTION_THREAD_SHARE_MODE
                                    + " must in [ALL, OFF, PART]");
                }
                engineConfig.setTaskExecutionThreadShareMode(ThreadShareMode.valueOf(mode));
            } else if (ServerConfigOptions.SLOT_SERVICE.key().equals(name)) {
                engineConfig.setSlotServiceConfig(parseSlotServiceConfig(node));
            } else if (ServerConfigOptions.CHECKPOINT.key().equals(name)) {
                engineConfig.setCheckpointConfig(parseCheckpointConfig(node));
            } else {
                LOGGER.warning("Unrecognized element: " + name);
            }
        }
    }

    private CheckpointConfig parseCheckpointConfig(Node checkpointNode) {
        CheckpointConfig checkpointConfig = new CheckpointConfig();
        for (Node node : childElements(checkpointNode)) {
            String name = cleanNodeName(node);
            if (ServerConfigOptions.CHECKPOINT_INTERVAL.key().equals(name)) {
                checkpointConfig.setCheckpointInterval(
                        getIntegerValue(
                                ServerConfigOptions.CHECKPOINT_INTERVAL.key(),
                                getTextContent(node)));
            } else if (ServerConfigOptions.CHECKPOINT_TIMEOUT.key().equals(name)) {
                checkpointConfig.setCheckpointTimeout(
                        getIntegerValue(
                                ServerConfigOptions.CHECKPOINT_TIMEOUT.key(),
                                getTextContent(node)));
            } else if (ServerConfigOptions.CHECKPOINT_MAX_CONCURRENT.key().equals(name)) {
                checkpointConfig.setMaxConcurrentCheckpoints(
                        getIntegerValue(
                                ServerConfigOptions.CHECKPOINT_MAX_CONCURRENT.key(),
                                getTextContent(node)));
            } else if (ServerConfigOptions.CHECKPOINT_TOLERABLE_FAILURE.key().equals(name)) {
                checkpointConfig.setTolerableFailureCheckpoints(
                        getIntegerValue(
                                ServerConfigOptions.CHECKPOINT_TOLERABLE_FAILURE.key(),
                                getTextContent(node)));
            } else if (ServerConfigOptions.CHECKPOINT_STORAGE.key().equals(name)) {
                checkpointConfig.setStorage(parseCheckpointStorageConfig(node));
            } else {
                LOGGER.warning("Unrecognized element: " + name);
            }
        }

        return checkpointConfig;
    }

    private CheckpointStorageConfig parseCheckpointStorageConfig(Node checkpointStorageConfigNode) {
        CheckpointStorageConfig checkpointStorageConfig = new CheckpointStorageConfig();
        for (Node node : childElements(checkpointStorageConfigNode)) {
            String name = cleanNodeName(node);
            if (ServerConfigOptions.CHECKPOINT_STORAGE_TYPE.key().equals(name)) {
                checkpointStorageConfig.setStorage(getTextContent(node));
            } else if (ServerConfigOptions.CHECKPOINT_STORAGE_MAX_RETAINED.key().equals(name)) {
                checkpointStorageConfig.setMaxRetainedCheckpoints(
                        getIntegerValue(
                                ServerConfigOptions.CHECKPOINT_STORAGE_MAX_RETAINED.key(),
                                getTextContent(node)));
            } else if (ServerConfigOptions.CHECKPOINT_STORAGE_PLUGIN_CONFIG.key().equals(name)) {
                Map<String, String> pluginConfig = parseCheckpointPluginConfig(node);
                checkpointStorageConfig.setStoragePluginConfig(pluginConfig);
            } else {
                LOGGER.warning("Unrecognized element: " + name);
            }
        }
        return checkpointStorageConfig;
    }

    /**
     * Parse checkpoint plugin config.
     *
     * @param checkpointPluginConfigNode checkpoint plugin config node
     * @return checkpoint plugin config
     */
    private Map<String, String> parseCheckpointPluginConfig(Node checkpointPluginConfigNode) {
        Map<String, String> checkpointPluginConfig = new HashMap<>();
        for (Node node : childElements(checkpointPluginConfigNode)) {
            String name = node.getNodeName();
            checkpointPluginConfig.put(name, getTextContent(node));
        }
        return checkpointPluginConfig;
    }
}
