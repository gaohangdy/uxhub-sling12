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

    <!-- <div class="row" style="border: solid silver 2px; box-shadow: 3px 3px 4px lightgray; margin-right: 10px;"> -->
    <div class="row">
        <template v-if="!this.$root.$data.state.userPreferences.isTenant">
            <div class="tenant-tabs">
                <button v-for="(item, index) in tab.items"
                    class="tab"
                    :key="item"
                    :class="{active: tab.active === index}"
                    @click="onTabClick(index)">
                    {{ item }}
                </button>
            </div>
            <div class="tenant-collection">
                <template v-if="children && children.length > 0">
                    <div class="col s6 m4 l3 icon-action m-left-inherit" v-for="child in children" v-bind:key="child.name">
                        <div class="card tenant-link" @click="onCardContentClick(child.name)">
                            <div class="card-image">
                                <div class="card-image-animation" v-bind:style="{backgroundImage: `url('${child.siteimage}')`}" />
                            </div>
                            <div class="card-content">
                                <span class="card-title">{{child.title ? child.title : child.name}}</span>
                                <p v-if="child.description">{{child.description}}</p>
                            </div>
                            <div class="card-action">
                                <admin-components-action
                                    v-bind:model="{
                                        target: { 
                                            path: '/content/admin/pages/welcome.html/pages:/content/' + child.name, 
                                            name: child.name 
                                        },
                                        command: 'selectTenant',
                                        tooltipTitle: `${$i18n('edit')} '${child.title || child.name}'`,
                                        type: 'icon'
                                    }">
                                    <i class="material-icons">edit</i>
                                </admin-components-action>

                                <admin-components-action
                                    v-bind:model="{
                                        target: { 
                                            path: '/content/admin/pages/tenants/configure.html/path:/content/' + child.name,
                                            name: child.name 
                                        },
                                        command: 'configureTenant',
                                        tooltipTitle: `${$i18n('configure')} '${child.title || child.name}'`,
                                        type: 'icon'
                                    }">
                                    <i class="material-icons">settings</i>
                                </admin-components-action>


                                <admin-components-action
                                    v-bind:model="{
                                        target: { 
                                            path: '/content/admin/pages/index.html',
                                            name: child.name 
                                        },
                                        command: 'deleteTenant',
                                        tooltipTitle: `${$i18n('delete')} '${child.title || child.name}'`,
                                        type: 'icon'
                                    }">
                                    <i class="material-icons">delete</i>
                                </admin-components-action>
                            </div>
                        </div>                    
                        <!-- <div class="card blue-grey darken-3">
                            <div class="card-content white-text tenant-link" @click="onCardContentClick(child.name)">
                                <span class="card-title">{{child.title ? child.title : child.name}}</span>
                                <p>{{child.description}}</p>
                            </div>
                            <div class="card-action">
                                <admin-components-action
                                    v-bind:model="{
                                        target: { 
                                            path: '/content/admin/pages/welcome.html/pages:/content/' + child.name, 
                                            name: child.name 
                                        },
                                        command: 'selectTenant',
                                        tooltipTitle: `${$i18n('edit')} '${child.title || child.name}'`
                                    }">
                                    <i class="material-icons">edit</i>
                                </admin-components-action>

                                <admin-components-action
                                    v-bind:model="{
                                        target: { 
                                            path: '/content/admin/pages/tenants/configure.html/path:/content/' + child.name,
                                            name: child.name 
                                        },
                                        command: 'configureTenant',
                                        tooltipTitle: `${$i18n('configure')} '${child.title || child.name}'`
                                    }">
                                    <i class="material-icons">settings</i>
                                </admin-components-action>


                                <admin-components-action
                                    v-bind:model="{
                                        target: { 
                                            path: '/content/admin/pages/index.html',
                                            name: child.name 
                                        },
                                        command: 'deleteTenant',
                                        tooltipTitle: `${$i18n('delete')} '${child.title || child.name}'`
                                    }">
                                    <i class="material-icons">delete</i>
                                </admin-components-action>
                            </div>
                        </div> -->
                    </div>
                </template>
                <template v-else>
                    <div class="no-websites-found">
                        <p>No websites found</p>
                        Star
  components: { template },
  components: { template },t by creating a new one!
                    </div>
                </template>
            </div>
            <!-- <div v-if="tab.active === 0" class="tenant-actions">
                <div class="create-tenant action" @click="onCreateNewSiteClick">
                    <i class="material-icons">note_add</i>
                    Create new website
                </div>
            </div> -->
        </template>
        <template v-else>
            <h5 class="sub-title"><span>Owner</span></h5>
            <div class="tenant-collection">
                <template v-if="ownerChildren && ownerChildren.length > 0">
                    <template v-for="child in ownerChildren">
                        <template v-if="child.owner">
                            <div class="col s6 m4 l3 icon-action m-left-inherit" v-bind:key="child.name">
                                <div class="card tenant-link" @click="onCardContentClick(child.name)">
                                    <div class="card-image">
                                        <div class="card-image-animation" v-bind:style="{backgroundImage: `url('${child.siteimage}')`}" />
                                    </div>
                                    <div class="card-content">
                                        <span class="card-title">{{child.title ? child.title : child.name}}</span>
                                        <p v-if="child.description">{{child.description}}</p>
                                    </div>
                                    <div class="card-action">
                                        <admin-components-action
                                            v-bind:model="{
                                                target: { 
                                                    path: '/content/admin/pages/welcome.html/pages:/content/' + child.name, 
                                                    name: child.name 
                                                },
                                                command: 'selectTenant',
                                                tooltipTitle: `${$i18n('edit')} '${child.title || child.name}'`,
                                                type: 'icon'
                                            }">
                                            <i class="material-icons">edit</i>
                                        </admin-components-action>

                                        <admin-components-action
                                            v-bind:model="{
                                                target: { 
                                                    path: '/content/admin/pages/tenants/configure.html/path:/content/' + child.name,
                                                    name: child.name 
                                                },
                                                command: 'configureTenant',
                                                tooltipTitle: `${$i18n('configure')} '${child.title || child.name}'`,
                                                type: 'icon'
                                            }">
                                            <i class="material-icons">settings</i>
                                        </admin-components-action>


                                        <admin-components-action
                                            v-bind:model="{
                                                target: { 
                                                    path: '/content/admin/pages/index.html',
                                                    name: child.name 
                                                },
                                                command: 'deleteTenant',
                                                tooltipTitle: `${$i18n('delete')} '${child.title || child.name}'`,
                                                type: 'icon'
                                            }">
                                            <i class="material-icons">delete</i>
                                        </admin-components-action>
                                    </div>
                                </div>                    
                            </div>
                        </template>
                    </template>
                </template>
                <template v-else>
                    <div class="no-websites-found">
                        <p>No websites found</p>
                        Start by creating a new one!
                    </div>
                </template>
            </div>
            <h5 class="sub-title"><span>Contributor</span></h5>
            <div class="tenant-collection">
                <template v-if="contributorChildren && contributorChildren.length > 0">
                    <template v-for="child in contributorChildren">
                        <template v-if="!child.owner">
                            <div class="col s6 m4 l3 icon-action m-left-inherit" v-bind:key="child.name">
                                <div class="card tenant-link" @click="onCardContentClick(child.name)">
                                    <div class="card-image">
                                        <div class="card-image-animation" v-bind:style="{backgroundImage: `url('${child.siteimage}')`}" />
                                    </div>
                                    <div class="card-content">
                                        <span class="card-title">{{child.title ? child.title : child.name}}</span>
                                        <p v-if="child.description">{{child.description}}</p>
                                    </div>
                                    <div class="card-action">
                                        <admin-components-action
                                            v-bind:model="{
                                                target: { 
                                                    path: '/content/admin/pages/welcome.html/pages:/content/' + child.name, 
                                                    name: child.name 
                                                },
                                                command: 'selectTenant',
                                                tooltipTitle: `${$i18n('edit')} '${child.title || child.name}'`,
                                                type: 'icon'
                                            }">
                                            <i class="material-icons">edit</i>
                                        </admin-components-action>

                                        <admin-components-action
                                            v-bind:model="{
                                                target: { 
                                                    path: '/content/admin/pages/tenants/configure.html/path:/content/' + child.name,
                                                    name: child.name 
                                                },
                                                command: 'configureTenant',
                                                tooltipTitle: `${$i18n('configure')} '${child.title || child.name}'`,
                                                type: 'icon'
                                            }">
                                            <i class="material-icons">settings</i>
                                        </admin-components-action>


                                        <admin-components-action
                                            v-bind:model="{
                                                target: { 
                                                    path: '/content/admin/pages/index.html',
                                                    name: child.name 
                                                },
                                                command: 'deleteTenant',
                                                tooltipTitle: `${$i18n('delete')} '${child.title || child.name}'`,
                                                type: 'icon'
                                            }">
                                            <i class="material-icons">delete</i>
                                        </admin-components-action>
                                    </div>
                                </div>                    
                            </div>
                        </template>
                    </template>
                </template>
                <template v-else>
                    <div class="no-websites-found">
                        <p>No websites found</p>
                        Start by creating a new one!
                    </div>
                </template>
            </div>
        </template>
    </div>

</template>

<script>
    export default {
        props: ['model'],
        data(){
            return {
                isDraggingFile: false,
                isDraggingUiEl: false,
                isFileUploadVisible: false,
                uploadProgress: 0,
                tab: {
                    active: 0,
                    items: ['Websites', 'Website Themes', 'Internal Websites']
                }
            }
        },
        computed: {
            children: function() {
                const tenants = $perAdminApp.getNodeFrom($perAdminApp.getView(), '/admin/tenants')
                if(tenants) {
                    if (this.$root.$data.state.userPreferences.isTenant) {
                        return tenants.filter( (t) => !t.template && !t.internal)
                    } else {
                        if (this.tab.active === 1) {
                            return tenants.filter( (t) => t.template)
                        } else if (this.tab.active === 2) {
                            return tenants.filter( (t) => t.internal );
                        } else {
                            return tenants.filter( (t) => !t.template && !t.internal)
                        }
                    }
                }
                return [];
            },
            ownerChildren: function() {
                const tenants = $perAdminApp.getNodeFrom($perAdminApp.getView(), '/admin/tenants')
                if(tenants) {
                    return tenants.filter( (t) => !t.template && !t.internal && t.owner)
                }
                return [];                
            },
            contributorChildren: function() {
                const tenants = $perAdminApp.getNodeFrom($perAdminApp.getView(), '/admin/tenants')
                if(tenants) {
                    return tenants.filter( (t) => !t.template && !t.internal && !t.owner)
                }
                return [];                
            }
        },
        created() {
        },
        methods: {
            selectTenant(vm, target) {
                $perAdminApp.stateAction('setTenant', { name: target.name }).then( () => {
                    $perAdminApp.loadContent(`/content/admin/pages/welcome.html/path:/content/${target.name}`)
                });
            },

            deleteTenant: function(me, target) {
                $perAdminApp.askUser('Delete Site', me.$i18n('Are you sure you want to delete this site, its children, and generated content and components?'), {
                    yes() {
                        $perAdminApp.stateAction('deleteTenant', target)
                    }
                })
            },

            onCardContentClick(name) {
                this.selectTenant(this, { name })
            },

            onCreateNewSiteClick() {
                $perAdminApp.action(this, 'selectPath', '/content/admin/pages/pages/createtenant')
            },

            onTabClick(index) {
                this.tab.active = index
            }
        }
    }
</script>

<style scoped>
    fieldset {
        border: none;
    }

    .tenant-link:hover {
        background-color: rgba(255, 255, 255, .05);
        cursor: pointer;
    }

    .card-action {
        display: flex;
        justify-content: space-between;
        flex-direction: column;
        position: absolute;
        top: 0;
        right: 0;
        height: 200px;
        background-color: unset;

        -webkit-transition: all 1.5s;  
        -moz-transition: all 1.5s;  
        -ms-transition: all 1.5s;  
        -o-transition: all 1.5s;  
        transition: all 1.5s;  
        opacity: 0;
    }

    .tenant-tabs,
    .tenant-actions{
        width: 100%;
        display: flex;
        justify-content: flex-start;
        align-items: center;
        height: 50px;
        /* background-color: #eeeeee; */
    }

    .tenant-tabs {
        border-bottom: 1px solid silver;
    }

    .tenant-tabs .tab {
        height: 100%;
        width: 33.333333333%;
        justify-content: center;
        align-items: center;
        display: flex;
        cursor: pointer;
        transition: background-color .35s ease-out;
        border: none;
    }

    .tenant-tabs .tab:hover,
    .tenant-tabs .tab.active {
        background-color: rgba(0, 0, 0, .1);
    }

    .tenant-actions {
        border-top: 2px solid silver;
        display: flex;
        justify-content: center;
    }

    .tenant-actions .action {
        width: 250px;
        height: 90%;
        display: flex;
        justify-content: center;
        align-items: center;
        color: #ffff;
        background-color: rgba(55, 71, 79, 1);
        cursor: pointer;
        border-radius: 3px;
    }

    .tenant-actions .action:hover {
        background-color: rgba(55, 71, 79, .92);
    }

    .tenant-actions .action i.material-icons {
        color: #ffab40;
        margin-right: 10px;
    }

    .tenant-collection {
        min-height: 272px;
        display: flex;
        flex-wrap: wrap;
    }

    .tenant-collection .card {
        min-height: 250px;
    }

    .tenant-collection .card .card-content {
        min-height: 64px;
        padding: 12px;
    }

    .tenant-collection .no-websites-found {
        width: 100%;
        display: flex;
        justify-content: center;
        align-items: center;
        flex-direction: column;
        color: #b4b4b4;
        font-size: 12px;
        font-weight: 400;
    }

    .no-websites-found p {
        font-size: 20px;
    }

    .m-left-inherit {
        margin-left: inherit !important;
    }

    .card-image {
        height: 200px;     
        overflow: hidden;   
    }

    .card.tenant-link:hover .card-image-animation {
        transform: scale(1.1)
    }    

    .card.tenant-link .card-image-animation {
        width: auto;
        min-height: 200px;
        transition: transform .5s;
        backface-visibility: hidden;
        -webkit-backface-visibility: hidden;
        background-position: center;
        background-repeat: no-repeat;
        background-size: cover;
    }  
    
    .card.tenant-link:hover .card-action {
        -webkit-transition: all 1s;  
        -moz-transition: all 1s;  
        -ms-transition: all 1s;  
        -o-transition: all 1s;  
        transition: all 1s;  
        opacity: 1;
    }   
    
    .sub-title {
        margin: 36px 0 18px;
        border-left: 4px solid #b60081;
        font-weight: 700;
    }

    .sub-title span {
        padding: 3px 0 0 10px;
        display: block;
        border-left: 2px solid #e9b3d9;        
    }
</style>