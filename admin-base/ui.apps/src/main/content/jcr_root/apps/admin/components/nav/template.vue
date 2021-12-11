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
  <nav v-bind:data-per-path="model.path" v-bind:class="isExtended ? 'nav-extended' : ''">
    <!-- <div class="nav-wrapper blue-grey darken-3"> -->
    <div class="nav-wrapper">
      <div class="nav-left">
        <div class="brand-logo" @click="'/content/admin/pages/index.html'">
          <admin-components-logo/>
        </div>
        <admin-components-materializedropdown
            v-if="!model.hideTenants && state.tenant"
            class="current-tenant-name nav-link"
            :class="{active: getActiveSection() === 'tenants'}"
            :below-origin="true"
            :items="tenantDdItems">
          {{ vTenantTitle }}
          <i class="material-icons large" style="padding-left: 5px;">expand_more</i>
        </admin-components-materializedropdown>
      </div>
      <div class="nav-center">
        <ul v-if="!hideTenants" class="hide-on-med-and-down nav-mobile">
          <admin-components-action
              v-for="section in sections"
              :key="`section-${section.name}`"
              tag="li"
              :model="getSectionModel(section)"
              :class="{active: getActiveSection() === section.name, 'no-mobile': !section.mobile}"
              class="nav-link"/>
        </ul>
        <ul v-else class="hide-on-med-and-down nav-mobile">
          <admin-components-action
              tag="li"
              :model="getSectionModel({title: $i18n('Sites'), name: 'index'})"
              :class="{active: getActiveSection() === 'index'}"
              class="nav-link"/>
        </ul>
      </div>
      <ul class="nav-right hide-on-med-and-down nav-mobile">
        <admin-components-materializemodal ref="languageModal">
          <template>
            <vue-multiselect
                :value="language"
                deselect-label=""
                track-by="name"
                label="title"
                placeholder="Language"
                :options="languages"
                :searchable="false"
                :allow-empty="false"
                @select="onSelectLang"/>
            <div class="modal-spacer"></div>
          </template>
          <template slot="footer">
            <button class="btn btn-primary" @click="$refs.languageModal.close()">
              {{ $i18n('Close') }}
            </button>
          </template>
        </admin-components-materializemodal>
        <admin-components-materializedropdown
            tag="li"
            class="nav-link notifications-link"
            :below-origin="true"
            :items="userDdItems">
          <template>
            <i class="material-icons large">notifications</i>
          </template>
        </admin-components-materializedropdown>        
        <admin-components-materializedropdown
            tag="li"
            class="nav-link user-link"
            :below-origin="true"
            :items="userDdItems">
          <template>
            <div class="user-circle">
              <i class="material-icons large">person</i>
            </div>
          </template>
          <template slot="header">
            <div class="user-circle big" :title="$i18n('profile picture')">
              <i class="material-icons">face</i>
            </div>
            <div class="user-info">
              <div class="row logged-in-as">
                <p class="bold">Logged in as:</p>
                <icon
                    icon="clone"
                    lib="font-awesome"
                    class="copy-username"
                    title="Copy username"
                    @click.native.stop.prevent="copyUsername"/>
                <div class="username" :title="username">
                  {{ username }}
                </div>
                <input ref="usernameInput" class="username-input" type="hidden" :value="username">
              </div>
              <div class="row">
                <p class="bold">Language:</p>
                {{ language.name }}
              </div>
              <div class="row">
                <p class="bold">Timezone:</p>
                {{ gmtOffset }}
              </div>
            </div>
          </template>
        </admin-components-materializedropdown>
        <!-- <admin-components-materializedropdown
            tag="li"
            class="nav-link more-link"
            :below-origin="true"
            :gutter="2"
            :items="moreDdItems">
          <i class="material-icons">more_vert</i>
        </admin-components-materializedropdown> -->
      </ul>
    </div>
    <template v-for="child in model.children">
      <component v-bind:is="child.component" v-bind:model="child"
                 v-bind:key="child.path"></component>
    </template>
  </nav>
</template>

<script>
import Icon from '../icon/template.vue'
import {Toast} from '../../../../../../js/constants'

export default {
  components: {
    Icon
  },
  props: ['model'],
  data() {
    return {
      state: $perAdminApp.getView().state,
      tenants: $perAdminApp.getView().admin.tenants || [],
      // language: {},
      languages: [],
      sections: [
        {name: 'welcome', title: 'Dashboard', mobile: true},
        {name: 'pages', title: 'Pages'},
        {name: 'assets', title: 'Assets'},
        {name: 'objects', title: 'Objects'},
        {name: 'templates', title: 'Templates'},
      ],
      helpSelection: 'Help'
    }
  },
  computed: {
    hideTenants() {
      return this.model.hideTenants ? true : $perAdminApp.getView().state.tenant ? false : true
    },
    language() {  
      return $perAdminApp.getView().state.language
      // return {name: $perAdminApp.getView().state.language}
    },
    // languages() {
    //   // return this.$i18nGetLanguages()
    // },
    vueRoot() {
      return this.$root
    },
    isExtended() {
      return this.model.children && this.model.children.length > 0
    },
    userPreferences() {
      return this.$root.$data.state.userPreferences
    },
    username() {
      if (this.userPreferences && this.userPreferences.profile
          && this.userPreferences.profile.email) {
        return this.userPreferences.profile.email
      }
      return this.$root.$data.state.user
    },
    help() {
      if ($perAdminApp.getView()) {
        return $perAdminApp.findNodeFromPath($perAdminApp.getView().adminPage,
            '/jcr:content/tour')
      }
    },
    moreDdItems() {
      return [
        {label: 'help', icon: 'help', disabled: !this.help, click: this.onHelpClick},
        {label: 'tutorials', icon: 'import_contacts', disabled: true, click: this.onTutorialsClick},
        {label: '--------------------', disabled: true},
        {label: this.$i18n('aboutNavBtn'), icon: 'info', click: this.onAboutClick},
      ]
    },
    tenantDdItems() {
      return [
        {label: 'Settings', icon: 'settings', click: this.onSettingsClick},
        {label: 'Change website', icon: 'swap_horiz', click: this.onChangeWebsiteClick}
      ]
    },
    userDdItems() {
      return [
        {label: 'Profile', icon: 'account_box', disabled: true},
        {label: 'Language', icon: 'language', click: this.onLanguageClick},
        {label: '', disabled: true},
        {label: 'Logout', icon: 'exit_to_app', click: this.onLogoutClick}
      ]
    },
    gmtOffset() {
      const offsetInMinutes = new Date().getTimezoneOffset()
      const offset = offsetInMinutes / 60 * -1
      const algebraicSign = offset >= 0 ? '+' : '-'
      return `GMT${algebraicSign}${offset}`
    },
    vTenantTitle() {
      return this.state.tenant.title || this.state.tenant.name
    }
  },
  beforeCreate() {
    $perAdminApp.getApi().populateTenants().then(() => {
      this.refreshTenants()
    })
    $perAdminApp.getApi().fetchLanguages().then((data) => {
      this.refreshLanguages(data)
    }) 
  },
  methods: {
    getSectionModel(section) {
      let target = `/content/admin/pages/${section.name}.html`
      if (this.state.tenant) {
        if (section.name !== 'welcome' && section.name !== 'object-definitions') {
          const path = this.state.tools[section.name]
          target += path && path.length > 0 ? `/path:${path}`
              : `/path:${this.state.tenant.roots[section.name]}`
        } else if (section.name === 'object-definitions') {
          target += `/path:/content/${this.state.tenant.name}/${section.name}`
        } 
        else {
          target += `/path:/content/${this.state.tenant.name}`
        }
      } else {
        target = '/content/admin/pages/index'
      }
      return {
        command: 'selectPath',
        title: this.$i18n(section.title),
        target
      }
    },
    onSelectLang({name, title}) {
      this.$i18nSetLanguage({name: name, title: title})
      $perAdminApp.forceFullRedraw()
    },
    onSelectTenant(tenant) {
      $perAdminApp.stateAction('setTenant', tenant)
    },
    onHelpClick() {
      $perAdminApp.action(this, 'showTour', '')
    },
    onTutorialsClick() {
      document.getElementById('peregrine-main').classList.toggle('tutorial-visible')
    },
    onAboutClick() {
      $('#aboutPeregrine').modal('open');
    },
    onSettingsClick() {
      $perAdminApp.action(this, 'configureTenant', {
        path: '/content/admin/pages/tenants/configure.html/content/' + this.state.tenant.name,
        name: this.state.tenant.name
      })
    },
    onChangeWebsiteClick() {
      $perAdminApp.action(this, 'selectPath', '/content/admin/pages/index')
    },
    onLogoutClick() {
      window.location.href = '/system/sling/logout?resource=/index.html';
    },
    onLanguageClick() {
      this.$refs.languageModal.open();
    },
    refreshTenants() {
      this.tenants = $perAdminApp.getView().admin.tenants || []
      this.state = $perAdminApp.getView().state
    },
    refreshLanguages(data) {
      this.languages = data
      // const currentLanguage = this.languages.filter((ch) => {
      //   return $perAdminApp.getView().state.language.name === ch.name
      // })  
      // this.language = currentLanguage[0]  

    },
    getActiveSection() {
      const breadcrumbs = $perAdminApp.getView().adminPage.breadcrumbs
      if (breadcrumbs) {
        return breadcrumbs[0].path.split('/')[4]
      }
      return 'welcome'
    },
    copyUsername() {
      let success = false
      this.$refs.usernameInput.setAttribute('type', 'text')
      this.$refs.usernameInput.select()
      try {
        success = !!document.execCommand('copy')
      } catch (err) {
        console.error('error while copying username: ', err)
      }
      if (success) {
        $perAdminApp.toast(`copied username <i>"${this.username}"</i>`, Toast.Level.INFO)
      } else {
        $perAdminApp.toast('FAILED to copy username', Toast.Level.WARNING)
      }
      this.$refs.usernameInput.setAttribute('type', 'hidden')
      window.getSelection().removeAllRanges()
    }
  }
}
</script>

<style scoped>
  .nav-extended {
    background-color: #F0F0F0;
    border-bottom: 0.0625rem solid #E1E1E1;
    box-shadow: unset;
  }
</style>