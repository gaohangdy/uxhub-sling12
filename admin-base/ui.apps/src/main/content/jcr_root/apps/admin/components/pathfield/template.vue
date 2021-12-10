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
    <div class="pathfield">
        <!-- <template v-for="(item, index) in pathSegments">
            /&nbsp;<admin-components-action
                v-bind:model="{
                    target: { path: item.path },
                    title: (index == 0)? $i18n(item.name) : item.name,
                    command: 'selectPathInNav',
                    classes: 'breadcrumb-link'
                }"></admin-components-action>&nbsp;
        </template> -->

        <admin-components-materializedropdown
            ref="dropdown"
            class="current-tenant-name nav-link"
            :below-origin="true">
            {{ pathSegments[pathSegments.length - 1].name }}
            <i class="material-icons large" style="padding-left: 5px;">expand_more</i>

            <template slot="content" v-if="pathSegments.length > 0">
                <li class="page-tree-item" v-for="(item, index) in pathSegments">
                    <admin-components-action
                        @actionClicked="closeDropdowndPanel"
                        v-bind:model="{
                            target: { path: item.path },
                            title: (index == 0)? $i18n(item.name) : item.name,
                            command: 'selectPathInNav',
                            classes: 'breadcrumb-link'
                        }"></admin-components-action>
                </li>
            </template>          
        </admin-components-materializedropdown>


    </div>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
            path: function() {
                var dataFrom = this.model.dataFrom
                var node = $perAdminApp.getNodeFrom(this.$root.$data, dataFrom)
                return node
            },
            pathSegments: function() {
                var segments = this.path.toString().split('/')
                var ret = []
                for(var i = 3; i < segments.length; i++) {
                    ret.push( { name: segments[i], path: segments.slice(0, i+1).join('/') } )
                }

                // for(var i = 3; i < segments.length; i++) {
                //     ret.push( { label: segments[i], click: this.onChangeWebsiteClick, target: segments.slice(0, i+1).join('/') } )
                // }                

                return ret;
            }
        },
        methods: {
            closeDropdowndPanel: function(e) {
                this.$refs.dropdown.close()
            }            
        }
    }
</script>

<style scoped>
  .pathfield {
    color: #707070;
    font-weight: 800;
    text-align: center;
    margin-left: auto;
  }

  .page-tree-item {
    list-style: none;
  }

  .page-tree-item span {
    padding: 0 16px;
  }
</style>
<style>
  .breadcrumb-link {
    color: #707070;
  }

  .pathfield a {
    display: flex;
    height: 48px;
    align-items: center;
    color: #707070;
    font-weight: 800;
    font-size: 20px;      
  }
</style>