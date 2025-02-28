<!--
  #%L
  admin base - UI Apps
  %%
  Copyright (C) 2017 headwire inc.
  %%
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at
  
  http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
  #L%
  -->
<template>
    <div class="nav-content sub-nav" :class="classes">
        <richtoolbar v-if="renderRichToolbar" class="on-sub-nav"/>
        <div v-if="showNodeTree" class="page-tree">
            <admin-components-materializedropdown
                ref="dropdown"
                :on-focus-out="() => {}"
                :below-origin="true"
                alignment="right">
                <template>
                    {{ vCurrentNodeTitle }}<span class="caret-down"></span>
                </template>
                <template slot="content" v-if="filteredChildren.length > 0">
                    <admin-components-nodetreeitem
                        v-for="(node, index) in filteredChildren"
                        :key="`page-tree-item-${node.path}`"
                        :item="node"
                        :supported-resource-types="NodeTree.SUPPORTED_RESOURCE_TYPES"
                        @click.native.stop="() => {}"
                        @edit-node="onTreeItemEditNode"/>
                </template>
            </admin-components-materializedropdown>
        </div>
        <template v-else v-for="child in model.children">
            <div v-bind:is="child.component" v-bind:model="child" v-bind:key="child.path"></div>
        </template>
    </div>
</template>

<script>
import {NodeTree} from '../../../../../../js/constants'
import {get} from '../../../../../../js/utils'
import Richtoolbar from '../richtoolbar/template.vue'

export default {
    components: {Richtoolbar},
    props: ['model'],
    data() {
        return {
            NodeTree: NodeTree
        }
    },
    computed: {
        classes() {
            if(this.model.classes) {
                return this.model.classes
            }
            return 'navright'
        },
        isEditPage() {
            return this.model.classes && this.model.classes.indexOf('navcenter') >= 0
        },
        tenant() {
            return $perAdminApp.getView().state.tenant
        },
        section() {
          return this.getPath().split('/')[3]
        },
        sectionRoot() {
            return this.tenant.roots[this.section]
        },
        showNodeTree() {
            return this.isEditPage && ['pages', 'templates'].indexOf(this.section) >= 0
        },
        renderRichToolbar() {
            return this.isEditPage && ['pages', 'templates'].indexOf(this.section) >= 0
        },
        nodes() {
            return $perAdminApp.getView().admin.nodes
        },
        nodeTreeRootNode() {
            try {
                if (this.showNodeTree) {
                    return $perAdminApp.findNodeFromPath(this.nodes, this.sectionRoot)
                } else {
                    return {}
                }
            } catch(err) {
                return {}
            }
        },
        filteredChildren() {
            if (this.nodeTreeRootNode && this.nodeTreeRootNode.children) {
                return this.nodeTreeRootNode.children.filter((ch) => {
                    return this.isSupportedNodeTreeResourceType(ch.resourceType)
                })
            } else {
                return []
            }
        },
        currentNode() {
          return $perAdminApp.findNodeFromPath(this.nodes, this.getPath())
              || {title: 'loading...'}
        },
      vCurrentNodeTitle() {
          return this.currentNode.title || this.currentNode.name
      }
    },
    methods: {
        isEditor() {
            return this.$root.$data.adminPage.title === 'editor'
        },
        getPath(){
            if( this.$root.$data.pageView){
                if( this.$root.$data.pageView.path ){
                    return this.$root.$data.pageView.path;
                } else {
                    return '';
                }
            } else {
                return '';
            }
        },
        getDownloadPath(){
            return this.getPath().split('/').reverse()[0];
        },
        onTreeItemEditNode() {
            this.$refs.dropdown.close()
        },
        isSupportedNodeTreeResourceType(resourceType) {
            return resourceType && NodeTree.SUPPORTED_RESOURCE_TYPES.indexOf(resourceType) >= 0
        },
        isRich() {
            return get($perAdminApp.getView(), '/state/inline/rich', false)
        }
    }
}
</script>

<style>
    .page-tree a {
        color: #707070;
    }
</style>

