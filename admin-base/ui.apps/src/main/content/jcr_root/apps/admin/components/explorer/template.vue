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
<div class="explorer"
    v-on:dragover.prevent  ="onDragOverExplorer"
    v-on:dragenter.prevent ="onDragEnterExplorer"
    v-on:dragleave.prevent ="onDragLeaveExplorer"
    v-on:drop.prevent      ="onDropExplorer">

    <div class="explorer-layout">
    <div class="row">
        <div v-if="pt" class="col s12 m8 explorer-main">
            <ul class="collection">
                <li v-if="showNavigateToParent"
                    v-on:click.stop.prevent="selectParent()"
                    class="collection-item">
                    <admin-components-action
                            v-bind:model="{
                            target: null,
                            command: 'selectParent',
                            tooltipTitle: $i18n('backToParentDir')
                        }"><i class="material-icons">folder_open</i><i class="material-icons">arrow_upward</i>
                    </admin-components-action>
                </li>
                <li
                    v-for ="child in children"
                    v-bind:key="child.path"
                    v-bind:class="`collection-item ${isSelected(child) ? 'explorer-item-selected' : ''}`"
                    draggable ="true"
                    v-on:dragstart ="onDragRowStart(child,$event)"
                    v-on:drag      ="onDragRow"
                    v-on:click     ="showRow(child,$event)"
                    v-on:dragend   ="onDragRowEnd(child,$event)"
                    v-on:dragenter.stop.prevent ="onDragEnterRow"
                    v-on:dragover.stop.prevent  ="onDragOverRow"
                    v-on:dragleave.stop.prevent ="onDragLeaveRow"
                    v-on:drop.prevent      ="onDropRow(child, $event)">

                    <admin-components-draghandle/>

                    <admin-components-action v-if="editable(child)"
                                             v-bind:model="{
                                target: child,
                                command: 'selectPath',
                                tooltipTitle: `${$i18n('select')} '${child.title || child.name}'`
                            }">
                        <i v-if="child.hasChildren" class="material-icons">folder</i>
                        <i v-else-if="!isInsideObjectDefinition(child.path) &&  child.resourceType !== 'nt:file'" class="material-icons">folder_open</i>
                    </admin-components-action>

                    <admin-components-action v-if="editable(child)"
                        v-bind:model="{
                            target: child,
                            command: 'editEntity',
                            dblClickTarget: child,
                            dblClickCommand: 'selectPath',
                            tooltipTitle: `${$i18n('edit')} '${child.title || child.name}'`
                        }">
                      <icon v-bind="nodeTypeToIcon(child)"/> {{child.title ? child.title : child.name}}
                    </admin-components-action>

                    <admin-components-action v-if="!editable(child)"
                        v-bind:model="{
                            target: child,
                            command: 'selectPath',
                            tooltipTitle: `${$i18n('select')} '${child.title || child.name}'`
                        }">
                      <icon v-bind="nodeTypeToIcon(child)"/> {{child.title ? child.title : child.name}}
                    </admin-components-action>

                    <admin-components-extensions v-bind:model="{id: 'admin.components.explorer', item: child}"></admin-components-extensions>

                    <div class="secondary-content">
                        <admin-components-action v-if="editable(child)"
                            v-bind:model="{
                                target: child,
                                command: 'editEntity',
                                tooltipTitle: `${$i18n('edit')} '${child.title || child.name}'`
                            }">
                            <admin-components-iconeditpage></admin-components-iconeditpage>
                        </admin-components-action>

                        <admin-components-action v-if="replicable(child)"
                            v-bind:model="{
                                target: child.path,
                                command: 'replicate',
                                tooltipTitle: `${$i18n('replicate')} '${child.title || child.name}'`
                            }">
                            <i class="material-icons" v-bind:class="replicatedClass(child)">public</i>
                        </admin-components-action>

                        <admin-components-action v-if="editable(child)"
                            v-bind:model="{
                                target: child,
                                command: 'showInfo',
                                tooltipTitle: `'${child.title || child.name}' ${$i18n('info')}`
                            }">
                            <i class="material-icons">info</i>
                        </admin-components-action>

                        <span v-if="viewable(child)">
                            <a
                                target      ="viewer"
                                v-bind:href ="viewUrl(child)"
                                v-on:click.stop  =""
                                v-bind:title="`${$i18n('view')} '${child.title || child.name}' ${$i18n('inNewTab')}`"
                                >
                                <i class="material-icons">visibility</i>
                            </a>
                        </span>

                        <admin-components-action
                            v-bind:model="{
                                target: child,
                                command: 'deleteTenantOrPage',
                                tooltipTitle: `${$i18n('delete')} '${child.title || child.name}'`
                            }">
                            <i class="material-icons">{{canBeDeleted(child) ? 'delete' : 'delete_forever'}}</i>
                        </admin-components-action>
                    </div>
                </li>
                <li class="collection-item" v-if="isAssets(path)">
                    <admin-components-action
                        v-bind:model="{
                            target: '',
                            command: 'addFolder',
                            tooltipTitle: `${$i18n('add folder')}`
                        }">
                            <i class="material-icons">add_circle</i> {{$i18n('add folder')}}
                    </admin-components-action>
                </li>
                <li class="collection-item" v-if="isPages(path)">
                    <admin-components-action
                        v-bind:model="{
                            target: '',
                            command: 'addPage',
                            tooltipTitle: `${$i18n('add page')}`
                        }">
                            <i class="material-icons">add_circle</i> {{$i18n('add page')}}
                    </admin-components-action>
                </li>
                <li class="collection-item" v-if="isObjects(path)">
                    <admin-components-action
                        v-bind:model="{
                            target: '',
                            command: 'addObject',
                            tooltipTitle: `${$i18n('add object')}`
                        }">
                            <i class="material-icons">add_circle</i> {{$i18n('add object')}}
                    </admin-components-action>
                </li>
                <li class="collection-item" v-if="isTemplates(path)">
                    <admin-components-action
                        v-bind:model="{
                            target: '',
                            command: 'addTemplate',
                            tooltipTitle: `${$i18n('add template')}`
                        }">
                            <i class="material-icons">add_circle</i> {{$i18n('add template')}}
                    </admin-components-action>
                </li>
                <li class="collection-item" v-if="isObjectDefinitions(path)">
                    <admin-components-action
                        v-bind:model="{
                            target: '',
                            command: 'addObjectDefinition',
                            tooltipTitle: `${$i18n('add object definition')}`
                        }">
                            <i class="material-icons">add_circle</i> {{$i18n('add object definition')}}
                    </admin-components-action>
                </li>
                <li class="collection-item" v-if="isInsideObjectDefinition(path)">
                    <admin-components-action
                        v-bind:model="{
                            target: '',
                            command: 'addObjectDefinitionFile',
                            tooltipTitle: `${$i18n('add ui-schema')}`
                        }">
                            <i class="material-icons">add_circle</i> {{$i18n('add ui-schema')}}
                    </admin-components-action>
                </li>
            </ul>
            <div style="width: inherit; position: absolute; bottom: .5em;" v-if="model.showFilter ==='true'">
                <div style="padding-left: 3em; padding-right: 1em;">
                    <div class="switch">
                        <label>
                            <input type="checkbox" v-model="filter" style="direction: rtl;">
                            <span class="lever"></span>
                            filter secondary items
                        </label>
                    </div>
                </div>
            </div>
            <div v-if="children && children.length == 0" class="empty-explorer">
                <div v-if="path.includes('assets')">
                    {{ $i18n('emptyExplorerHintAssets') }}.
                </div>
                <div v-else>
                    {{ $i18n('emptyExplorerHint') }}...
                </div>
            </div>
        </div>
        <div v-else class="col s12 m8 explorer-main explorer-empty">
            <div>
                <span>{{ $i18n(`nothing to show`) }}</span>
            </div>
        </div>
        <admin-components-explorerpreview v-if="hasEdit">
            <component v-bind:is="model.children[0].component" v-bind:model="model.children[0]"
                :onDelete="handleDelete"></component>
        </admin-components-explorerpreview>
    </div>
    </div>

    <div v-if="isFileUploadVisible" class="file-upload">
        <div class="file-upload-inner">
            <i class="material-icons">file_download</i>
            <span class="file-upload-text">Drag &amp; Drop files anywhere</span>
            <div class="progress-bar">
                <div class="progress-bar-value" v-bind:style="`width: ${uploadProgress}%`"></div>
            </div>
            <div class="progress-text">{{uploadProgress}}%</div>
            <div class="file-upload-action">
                <button
                    type="button"
                    class="btn"
                    v-on:click="onDoneFileUpload">ok</button>
            </div>
        </div>
    </div>

      <admin-components-publishingmodal
          v-if="publishDialogPath"
          :isOpen="publishDialogPath"
          :path="publishDialogPath"
          :modalTitle="`Web Publishing: ${publishDialogPath.split('/').pop()}`"
          @complete="closePublishing" />
</div>
</template>

<script>

import {getCurrentDateTime, set, get} from '../../../../../../js/utils'
import {IconLib} from '../../../../../../js/constants'

import Icon from '../icon/template.vue'

export default {
  components: {Icon},
  props: ['model'],

        data() {
            return {
                isDraggingFile: false,
                isDraggingUiEl: false,
                isFileUploadVisible: false,
                uploadProgress: 0,
                filter: true,
                publishDialogPath: null
            }
        },

        computed: {
            showNavigateToParent() {
                return this.path.split('/').length > 4
            },
            path: function() {
                var dataFrom = this.model.dataFrom
                var node = $perAdminApp.getNodeFrom($perAdminApp.getView(), dataFrom)
                return node
            },
            pt: function() {
                var node = this.path
                return $perAdminApp.findNodeFromPath(this.$root.$data.admin.nodes, node)
            },
            children: function() {
                if ( this.pt.children ) {
                    return this.pt.children.filter( child => this.checkIfAllowed(child) )
                }
            },
            parentPath: function() {
                var segments = this.$data.path.value.toString().split('/')
                var joined = segments.slice(0, segments.length - 1).join('/')
                return joined
            },
            pathSegments: function() {
                var segments = this.path.toString().split('/')
                var ret = []
                for(var i = 1; i < segments.length; i++) {
                    ret.push( { name: segments[i], path: segments.slice(0, i+1).join('/') } )
                }
                return ret;
            },
            hasEdit: function() {
                return this.model.children && this.model.children[0]
            },
        },
        created() {
          document.addEventListener('paste', this.onDocumentPaste)
        },
        beforeDestroy() {
          document.removeEventListener('paste', this.onDocumentPaste)
        },
        methods: {
            getTenant() {
              return $perAdminApp.getView().state.tenant || {name: 'example'}
            },

            isAssets(path) {
                return path.startsWith(`/content/${this.getTenant().name}/assets`)
            },

            isPages(path) {
                return path.startsWith(`/content/${this.getTenant().name}/pages`)
            },

            isObjects(path) {
                return path.startsWith(`/content/${this.getTenant().name}/objects`)
            },

            isObjectDefinitions(path) {
                return !this.isInsideObjectDefinition(path) 
                    && path.startsWith(`/content/${this.getTenant().name}/object-definitions`)
            },

            isInsideObjectDefinition(path) {
                return path.startsWith(`/content/${this.getTenant().name}/object-definitions/`);
            },

            isTemplates(path) {

                return path.startsWith(`/content/${this.getTenant().name}/templates`)
            },

            selectParent(me, target) {
                var dataFrom = !me ? this.model.dataFrom : me.model.dataFrom
                var path = $perAdminApp.getNodeFrom($perAdminApp.getView(), dataFrom)
                var pathSegments = path.split('/')
                pathSegments.pop()
                path = pathSegments.join('/')
                $perAdminApp.action(!me ? this: me, 'selectPath', { path: path, resourceType: 'sling:OrderedFolder'})
            },

            selectItem(item) {
                $perAdminApp.action(this, 'selectPath', item)
            },

            replicatedClass(item) {
                if(item.ReplicationStatus) {
                    const modified = item.lastModified || item.created
                    const replicated = item.Replicated
                    return `item-${item.ReplicationStatus}${replicated < modified ? '-modified' : ''}`
                }

                return 'item-replication-unknown'
            },

            replicate(me, path) {
                me.publishDialogPath = path
            },

            closePublishing() {
                this.publishDialogPath = null
            },

            isFolder(item) {
                return ["sling:OrderedFolder", "sling:Folder", "nt:folder"].indexOf(item.resourceType) >= 0
            },

            replicable(item) {
                return !this.isFolder(item)
            },

            onDragRowStart(item, ev) {
                ev.srcElement.classList.add("dragging");
                ev.dataTransfer.setData('text', item.path)
                if(this.isDraggingFile) { this.isDraggingFile = false }
                this.isDraggingUiEl = true
            },

            onDragRow(ev) {
                ev.srcElement.classList.remove("dragging");
            },

            onDragRowEnd(item, ev) {
                this.isDraggingUiEl = false
            },

            onDragOverRow(ev) {
                if(this.isDraggingUiEl) {
                    const center = ev.target.offsetHeight / 2 ;
                    this.dropType = ev.offsetY > center ? 'after' : 'before';
                    ev.target.classList.toggle('drop-after', ev.offsetY > center );
                    ev.target.classList.toggle('drop-before', ev.offsetY < center );
                }
            },

            onDragEnterRow(ev) {
            },

            onDragLeaveRow(ev) {
                if(this.isDraggingUiEl) {
                    ev.target.classList.remove('drop-after','drop-before')
                }
            },

            onDropRow(item, ev, type) {
                if(this.isDraggingUiEl) {
                    ev.target.classList.remove('drop-after','drop-before')
                    const dataFrom = this.model.dataFrom
                    const path = $perAdminApp.getNodeFrom($perAdminApp.getView(), dataFrom)
                    let action
                    switch(true) {
                        case (this.isPages(path)):
                            action = 'movePage'
                            break
                        case (this.isTemplates(path)):
                            action = 'moveTemplate'
                            break
                        case (this.isObjects(path)):
                            action = 'moveObject'
                            break
                        case (this.isAssets(path)):
                            action = 'moveAsset'
                            break
                        default:
                            console.warn('path is not a site, asset, template or object.')
                    }
                    $perAdminApp.stateAction(action, {
                        path: ev.dataTransfer.getData("text"),
                        to: item.path,
                        type: this.dropType
                    })
                }
            },

            onDragOverExplorer(ev) {
            },

            onDragEnterExplorer(ev) {
                if(!this.isAssets(this.path)) return
                if(this.isDraggingUiEl) return
                this.isDraggingFile = true
                this.isFileUploadVisible = true
            },

            onDragLeaveExplorer(ev) {
                /* hide upload overlay */
                /* TODO: fix upload unexpectedly closing
                if(this.isDraggingFile) {
                    this.isDraggingFile = false
                }
                */
            },

            onDropExplorer(ev) {
                if(!this.isAssets(this.path)) return
                if(this.isDraggingUiEl) return
                if(this.isDraggingFile) {
                    this.uploadFile(ev.dataTransfer.files)
                    this.isDraggingFile = false
                }
            },

            uploadFile(files) {
              $perAdminApp.stateAction('uploadFiles', {
                path: $perAdminApp.getView().state.tools.assets,
                files: files,
                cb: this.setUploadProgress
              })
            },

            setUploadProgress(percentCompleted) {
              this.uploadProgress = percentCompleted
            },

            onDoneFileUpload() {
                this.isFileUploadVisible = false
                this.uploadProgress = 0
            },

            isSelected: function(child) {
                if(this.model.selectionFrom && child) {
                    return $perAdminApp.getNodeFromViewOrNull(this.model.selectionFrom) === child.path
                } else if (child.resourceType) {
                    return child.path === $perAdminApp.getNodeFromViewOrNull('/state/tools/file');
                } else if(child.path === $perAdminApp.getNodeFromViewOrNull('/state/tools/page')) {
                    return true
                }
                return false

            },

            hasChildren: function(child) {
                return child && child.childCount && child.childCount > 0;
            },

            editable: function(child) {
                return ['per:Page', 'per:Object', 'nt:file'].indexOf(child.resourceType) >= 0
            },

            viewable: function(child) {
                return ['per:Page', 'per:Object', 'nt:file'].indexOf(child.resourceType) >= 0
            },

            viewUrl: function(child) {
                var path = child.path
                var segments = path.split('/')
                var last = segments.pop()
                if(last.indexOf('.') >= 0) {
                    return path
                }
                if(child.resourceType === 'per:Page') {
                    return path + '.html'
                }
                return path + '.json'
            },

          nodeTypeToIcon: function (item) {
            if (item.resourceType === 'per:Page') return {icon: 'description', lib: IconLib.MATERIAL_ICONS}
            if (item.resourceType === 'per:Object') return {icon: 'layers', lib: IconLib.MATERIAL_ICONS}
            if (item.resourceType === 'per:ObjectDefinition') return {
              icon: 'insert_drive_file',
              lib: IconLib.MATERIAL_ICONS
            }
            if (item.resourceType === 'nt:file') return this.fileExtToIcon(item)
            if (item.resourceType === 'per:Asset') return {icon: 'image', lib: IconLib.MATERIAL_ICONS}
            if (item.resourceType === 'sling:Folder') return {icon: 'folder', lib: IconLib.MATERIAL_ICONS}
            if (item.resourceType === 'sling:OrderedFolder') return {icon: 'folder', lib: IconLib.MATERIAL_ICONS}
            return {icon: '█', lib: IconLib.PLAIN_TEXT}
          },

          fileExtToIcon(item) {
            let ext = ''
            if (item.name) {
              ext = item.name.split('.').pop()
            }

            if (ext === 'json') {
              return {icon: '{&#8230;}', lib: IconLib.PLAIN_TEXT}
            } else {
              return {icon: 'insert_drive_file', lib: IconLib.MATERIAL_ICONS}
            }
          },

            checkIfAllowed: function(node) {
                if(this.model.showFilter && this.model.showFilter === 'true' && this.filter) {
                    if(node.excludeFromSitemap && node.excludeFromSitemap === 'true') return false
                    return ['per:Page', 'per:Asset', 'per:Object', 'per:ObjectDefinition'].indexOf(node.resourceType) >= 0
                }
                return ['per:Asset', 'nt:file', 'sling:Folder', 'sling:OrderedFolder', 'per:Page', 'sling:OrderedFolder', 'per:Object', 'per:ObjectDefinition'].indexOf(node.resourceType) >= 0
            },

            showInfo: function(me, {path, resourceType}) {
                const tenant = $perAdminApp.getView().state.tenant;
                const {model} = me;

                if (resourceType === 'nt:file') {
                    $perAdminApp.stateAction('selectFile', {path, resourceType});
                } else {
                    if(path.startsWith(`/content/${tenant.name}/objects`)) {
                        set($perAdminApp.getView(), `/state/tools/edit`, false);
                        $perAdminApp.stateAction('selectObject', { selected: path, path: model.dataFrom });
                    } else if (path.startsWith(`/content/${tenant.name}/templates`)) {
                        $perAdminApp.stateAction('showTemplateInfo', { selected: path });
                    } else {
                        $perAdminApp.stateAction('showPageInfo', { selected: path, resourceType });
                    }
                }
            },

            showRow: function(item, ev) {
                if (this.editable(item)) {  
                    this.showInfo(this, item);
                }
            },

            selectPath: function(me, target) {
                let resourceType = target.resourceType
                if(resourceType) {
                    if(resourceType === 'per:Object') {
                        $perAdminApp.stateAction('selectObject', { selected: target.path, path: me.model.dataFrom })
                        return
                    }
                    if(resourceType === 'per:Asset') {
                        $perAdminApp.stateAction('selectAsset', { selected: target.path })
                        return
                    } else if(resourceType === 'nt:file') {
                        return
                    }
                }
                if($perAdminApp.getNodeFromView('/state/tools/object/show')) {
                    $perAdminApp.stateAction('unselectObject', { })
                }
                if($perAdminApp.getNodeFromView('/state/tools/asset/show')) {
                    $perAdminApp.stateAction('unselectAsset', { })
                }

                $perAdminApp.stateAction('unselectFile')

                const payload = { selected: target.path, path: me.model.dataFrom }
                $perAdminApp.stateAction('selectToolsNodesPath', payload).then( () => {
                    $('div.brand-logo span').last().click() //TODO: quick and dirty solution!!!!
                })
            },

            selectPathInNav: function(me, target) {
                this.selectPath(me, target)
            },

            addSite: function(me, target) {
                $perAdminApp.stateAction('createTenantWizard', '/content')
            },

            addPage: function(me, target) {
                const tenant = $perAdminApp.getView().state.tenant
                const path = me.pt.path
                if(path.startsWith(`/content/${tenant.name}/pages`)) {
                    $perAdminApp.stateAction('createPageWizard', path)
                }
            },

            addTemplate: function(me, target) {
                const tenant = $perAdminApp.getView().state.tenant
                const path = me.pt.path
                if(path.startsWith(`/content/${tenant.name}/templates`)) {
                    $perAdminApp.stateAction('createTemplateWizard', path)
                }
            },

            addObject: function(me, target) {
                const tenant = $perAdminApp.getView().state.tenant
                const path = me.pt.path
                if(path.startsWith(`/content/${tenant.name}/objects`)) {
                    $perAdminApp.stateAction('createObjectWizard', { path: path, target: target })
                }
            },

            addObjectDefinition: function(me, target) {
                const tenant = $perAdminApp.getView().state.tenant
                const path = me.pt ? me.pt.path : `/content/${tenant.name}/object-definitions`
                if(path.startsWith(`/content/${tenant.name}/object-definitions`)) {
                    $perAdminApp.stateAction('createObjectDefinitionWizard', { path: path, target: target })
                }
            },

            addObjectDefinitionFile(me, target) {
                const tenant = $perAdminApp.getView().state.tenant;
                const path  = me.pt ? me.pt.path : `/content/${tenant.name}/object-definitions`;
                
                if (this.isInsideObjectDefinition(path)) {
                    $perAdminApp.stateAction('createObjectDefinitionFileWizard', {path, target});
                }
            },

            addFolder: function(me, target) {
                const tenant = $perAdminApp.getView().state.tenant
                const path = me.pt.path
                if(path.startsWith(`/content/${tenant.name}/assets`)) {
                    $perAdminApp.stateAction('createAssetFolderWizard', path)
                } else if(path.startsWith(`/content/${tenant.name}/objects`)) {
                    $perAdminApp.stateAction('createObjectFolderWizard', path)
                }
            },

            sourceImage: function(me, target) {
                $perAdminApp.stateAction('sourceImageWizard', me.pt.path)
            },

            canBeDeleted: function(obj) {
                return !(obj.activated || obj.anyDescendantActivated || obj.isReferenced);
            },

            deleteTenantOrPage: function(me, target) {
                if (target.activated) {
                    $perAdminApp.toast("The resource is still published. Please unpublish it first.", "warn", 7500)
                } else if (target.anyDescendantActivated) {
                    $perAdminApp.toast("One of the children of this resource is still published. Please unpublish all of them first.", "warn", 7500)
                } else if (target.isReferenced) {
                    $perAdminApp.toast("The resource is referenced somewhere. Please remove the references first.", "warn", 7500)
                } else if(me.path === '/content') {
                    me.deleteTenant(me, target)
                } else {
                    me.deletePage(me, target)
                }
            },

            handleDelete: function(type, path) {
                const me = this
                return new Promise((resolve, reject) => {
                    $perAdminApp.askUser(`Delete ${type}?`, me.$i18n(`Are you sure you want to delete this node and all its children?`), {
                        yes() {
                            $perAdminApp.stateAction(`delete${type.charAt(0).toUpperCase() + type.slice(1)}`, path)
                            resolve()
                        }
                    })
                })
            },

            deletePage: function(me, target) {
                const { resourceType, path } = target
                let type = 'folder'
                if (resourceType === 'per:Object') {
                    type = 'object'
                } else if(resourceType === 'per:Asset') {
                    type = 'asset'
                } else if(resourceType === 'per:Page') {
                    type = 'page'
                } else if(resourceType === 'nt:file') {
                    type = 'file'
                }

                this.handleDelete(type, path)
            },

            deleteTenant: function(me, target) {
                $perAdminApp.askUser('Delete Site', me.$i18n('Are you sure you want to delete this site, its children, and generated content and components?'), {
                    yes() {
                        $perAdminApp.stateAction('deleteTenant', target)
                    }
                })
            },

            editFile: function(me, target) {
                window.open(`/bin/cpm/edit/code.html${target}`, 'composum')
            },

            onDocumentPaste(event) {
              if (!this.path.includes('assets')) return

              const item = event.clipboardData.items[0]

              if (item && item.type.indexOf('image') === 0) {
                const blob = item.getAsFile()
                const extension = blob.type.split('/').pop()
                const name = `clipboard-${getCurrentDateTime()}.${extension}`
                const file = new File([blob], name, {type: blob.type})

                this.uploadFile([file])
              }
            }
        }
    }
</script>

<style>
    .item-activated {
        color: green;
    }

    .item-deactivated {
        color: red;
    }

    .item-activated-modified {
        color: purple;
    }

    .explorer-empty {
        display: flex;
        justify-content: center;
        align-items: center;
    }
</style>

<style scoped>
.icon.label {
  height: 24px;
  width: 24px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: bolder;
  color: #000000;
}
</style>