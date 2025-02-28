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
    <div class="tooling-page" v-if="allowRender">
        <template v-for="child in model.children">
            <component v-bind:is="child.component" v-bind:model="child" v-bind:key="child.path"></component>
        </template>
        <admin-components-about></admin-components-about>
        <admin-components-notifyuser></admin-components-notifyuser>
        <admin-components-askuser></admin-components-askuser>
        <admin-components-promptuser></admin-components-promptuser>
    </div>
</template>

<script>
import {set} from '../../../../../../js/utils'

export default {
    props: ['model'],
    mounted(){
        // init materialize plugins
        $('.modal').modal()
        if(this.preferences.firstLogin != undefined
                && this.preferences.firstLogin == 'true'
                && !window.location.pathname.startsWith('/content/admin/pages/onboard')) {
            // we should switch to this but currently we have a problem with the load and the url check
            // $perAdminApp.loadContent('/content/admin/pages/onboard.html')
            window.location = '/content/admin/pages/onboard.html'
        }
    },
    data() {
        return { decline: false, preferences: $perAdminApp.getNodeFromViewWithDefault('/state/userPreferences', { firstLogin: "true" }) }
    },
    computed: {
        allowRender() {
            if(this.preferences.firstLogin != undefined
                && this.preferences.firstLogin == 'true'
                && !window.location.pathname.startsWith('/content/admin/pages/onboard')) {
                return false
            }
            return true
        },
        path() {
          return $perAdminApp.getNodeFrom($perAdminApp.getView(), this.model.dataFrom)
        },
        pt() {
          return $perAdminApp.findNodeFromPath(this.$root.$data.admin.nodes, this.path)
        },
    },
    methods: {
        selectPath: function(me, target) {
            // const view = $perAdminApp.getView()
            // const tenant = view.state.tenant
            // const action = target.action || target
            // const section = action.split('/').slice(-1).pop()
            // set(view, '/state/current/section/name', section)
            // const payload = {
            //     path: `/state/tools/${section}`
            //     // ,
            //     // selected: `/content/${tenant.name}/${section}`
            // }
            // $perAdminApp.stateAction('selectToolsNodesPath', payload).then(() => {
            //     $perAdminApp.loadContent(action + '.html')
            // })
            if(target.indexOf('.html') >= 0) {
                $perAdminApp.loadContent(target)
            } else {
                $perAdminApp.loadContent(target + '.html')
            }
        },
        editPreview: function(me, target) {
            $perAdminApp.stateAction('editPreview', target)
        },
        editPage: function(me, target) {
        },
        addSite: function(me, target) {
            $perAdminApp.stateAction('createTenantWizard', '/content')
        },
        configureTenant: function(me, target) {
            $perAdminApp.stateAction('setTenant', target)
                .then(() => $perAdminApp.stateAction('configureTenant', target))
        },
        onDecline(me) {
            me.decline = true
            $perAdminApp.loadContent('/content/admin/pages/onboard/sorry.html')
        },
        onAccept(me) {
            $perAdminApp.stateAction('acceptTermsAndConditions', {} )
        },
        editReference(me, target) {
          if (target.load) {
            $perAdminApp.loadContent(target.load)
          } else {
            me.editEntity(me, target)
          }
        },
        
        editEntity(me, {path, resourceType}) {
          const view = $perAdminApp.getView()
          const tenant = view.state.tenant
          
          if (path.startsWith(`/content/${tenant.name}/pages`)) {
            set(view, '/state/tools/page', path)
          } else if (path.startsWith(`/content/${tenant.name}/templates`)) {
            set(view, '/state/tools/template', path)
          }

          if (resourceType === 'nt:file') {
            $perAdminApp.stateAction('editFile', {path, resourceType});
          } else if (path.startsWith(`/content/${tenant.name}/objects`)) {
            const node = $perAdminApp.findNodeFromPath($perAdminApp.getView().admin.nodes, path)
            $perAdminApp.stateAction('editObject', {selected: node.path, path: me.model.dataFrom})
          } else if (path.startsWith(`/content/${tenant.name}/templates`)) {
            $perAdminApp.stateAction('editTemplate', path)
          } else if (path.startsWith(`/content/${tenant.name}/object-definitions`)){
            $perAdminApp.stateAction('editObjectDefinitionFile', path);
          } else {
            $perAdminApp.stateAction('editPage', path);
        }
      }
    }
}
</script>
