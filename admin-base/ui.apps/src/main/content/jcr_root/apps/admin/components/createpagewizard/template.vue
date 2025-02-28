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
<div class="container">
    <form-wizard
      v-bind:title="'create a page'"
      v-bind:subtitle="''" @on-complete="onComplete"
      error-color="#d32f2f"
      color="#546e7a">
        <tab-content title="select template" :before-change="leaveTabOne">
            <fieldset class="vue-form-generator">
                <div class="form-group required">
                    <div class="row">
                        <div class="col s6">
                            <div v-if="skeletonPages && skeletonPages.length > 0" ref="testPanel">
                                <label class="as-heading">Select a Skeleton-Page</label>
                                <ul class="collection as-scrollable">
                                    <li class="collection-item"
                                        v-for="skeletonPage in skeletonPages"
                                        v-on:click.stop.prevent="selectSkeletonPage(null, skeletonPage.path)"
                                        v-bind:class="isSelected(skeletonPage.path) ? 'active' : ''"
                                        v-bind:key="skeletonPage.path">
                                        <admin-components-action v-bind:model="{ command: 'selectSkeletonPage', target: skeletonPage.path, title: skeletonPage.title ? skeletonPage.title : skeletonPage.name }"></admin-components-action>
                                    </li>
                                </ul>
                            </div>
                            <p v-if="skeletonPages && skeletonPages.length > 0">
                            </p>
                            <label class="as-heading">Select a Template</label>
                            <ul class="collection" v-bind:class="skeletonPages && skeletonPages.length > 0 ? 'as-scrollable-short' : 'as-scrollable'">
                                <li class="collection-item"
                                    v-for="template in templates"
                                    v-on:click.stop.prevent="selectTemplate(null, template.path)"
                                    v-bind:class="isSelected(template.path) ? 'active' : ''"
                                    v-bind:key="template.path">
                                    <admin-components-action v-bind:model="{ command: 'selectTemplate', target: template.path, title: template.title ? template.title : template.name }"></admin-components-action>
                                </li>
                            </ul>
                            <div v-if="formErrors.unselectedTemplateError" class="errors">
                                <span track-by="index">selection required</span>
                            </div>
                        </div>
                        <div class="col s6">
                            <label class="as-heading">Preview</label>
                            <p>
                                The area below shows a preview of the selected page type.
                            </p>
                            <div class="iframe-container" ref="pageThumbnail">
                                <iframe v-if="formmodel.skeletonPagePath"
                                        v-bind:src="formmodel.skeletonPagePath + '.html'" data-per-mode="preview" ref="iframeOne">
                                </iframe>
                                <iframe v-if="formmodel.templatePath"
                                        v-bind:src="formmodel.templatePath + '.html'" data-per-mode="preview">
                                </iframe>
                            </div>
                            <button type="button" class="wizard-btn outline" @click="onThumbnail()">
                                Capture
                            </button> 
                            <img v-if="thumbnailData" :src="thumbnailData" />                         
                        </div>
                    </div>
                </div>
            </fieldset>
        </tab-content>
        <tab-content title="choose name" :before-change="leaveTabTwo">
            <vue-form-generator
              :model   ="formmodel"
              :schema  ="nameSchema"
              :options ="formOptions"
              ref      ="nameTab">
            </vue-form-generator>
        </tab-content>
        <tab-content title="verify">
            Creating Page `{{formmodel.name}}` from
            <span v-if="formmodel.templatePath">template `{{formmodel.templatePath}}`</span>
            <span v-else-if="formmodel.skeletonPagePath">skeleton page `{{formmodel.skeletonPagePath}}`</span>
        </tab-content>
        <span v-if="isLastStep" slot="custom-buttons-right" role="button">
            <button type="button" class="wizard-btn outline" @click="onComplete(false)">
                Finish
           </button>
        </span>
        <span slot="finish" role="button" tabindex="0">
            <button tabindex="-1" type="button" class="wizard-btn finish">
            Finish and Edit!
            </button>
        </span>
    </form-wizard>
</div>
</template>

<script>
    import NodeNameValidation from '../../../../../../js/mixins/NodeNameValidation'

    // /* ES6 */
    // import * as htmlToImage from 'html-to-image';
    // import { toPng, toJpeg, toBlob, toPixelData, toSvg } from 'html-to-image';
    import html2canvas from 'html2canvas';

    export default {
        props: ['model'],
        data() {
            return {
                formErrors: {
                    unselectedTemplateError: false
                },
                isLastStep: false,
                thumbnailData: null
            }
        },
        created() {
            //By default select the first item in the list;
            if(this.skeletonPages && this.skeletonPages.length > 0) {
                this.selectSkeletonPage(this, this.skeletonPages[0].path);
            } else if(this.templates && this.templates.length > 0) {
                this.selectTemplate(this, this.templates[0].path);
            }
        },
        mixins: [NodeNameValidation],
        computed: {
            pageSchema() {
                if(this.formmodel.templatePath !== '') {
                    const definitions = $perAdminApp.getNodeFromView('/admin/componentDefinitions')
                    if(definitions) {
                        // todo: component should be resolved through the template
                        const comp = 'pagerendervue-structure-page'
                        const def = $perAdminApp.getNodeFromView('/admin/componentDefinitions')[comp]
                        return def
                    }
                }
            },
            templates() {
                const templates = $perAdminApp.getNodeFromViewOrNull('/admin/templates/data')
                const siteRootParts = this.formmodel.path.split('/').slice(0,4)
                siteRootParts[3] = 'templates'
                siteRootParts.push('')
                const siteRoot = siteRootParts.join('/')
                return templates.filter( (item) => item.path.startsWith(siteRoot))
            },
            skeletonPages() {
                const siteRoot = this.formmodel.path.split('/').slice(0,4).join('/') + '/skeleton-pages'
                const skeletonPageRoot = $perAdminApp.findNodeFromPath(this.$root.$data.admin.skeletonNodes, siteRoot)
                if(skeletonPageRoot) {
                    return skeletonPageRoot.children
                }
                return []
            }
        }
        ,
        methods: {
            selectTemplate(me, target){
                if(me === null) me = this
                me.formmodel.templatePath = target
                me.formmodel.skeletonPagePath = ''
                this.validateTabOne(me);
            },
            selectSkeletonPage(me, target){
                if(me === null) me = this
                me.formmodel.skeletonPagePath = target
                me.formmodel.templatePath = ''
                this.validateTabOne(me);
            },
            isSelected(target) {
                return this.formmodel.templatePath === target || this.formmodel.skeletonPagePath === target
            },
            onComplete(edit=true) {
                const payload = {
                    parent: this.formmodel.path,
                    name: this.formmodel.name,
                    template: this.formmodel.templatePath,
                    title: this.formmodel.title,
                    data: this.formmodel,
                    edit
                }

                if(this.formmodel.templatePath) {
                    payload.template = this.formmodel.templatePath
                    $perAdminApp.stateAction('createPage', payload)
                } else if (this.formmodel.skeletonPagePath) {
                    payload.skeletonPagePath = this.formmodel.skeletonPagePath
                    $perAdminApp.stateAction('createPageFromSkeletonPage', payload)
                } else {
                    throw 'error creating page: no template or skeleton page given!'
                }
            },
            validateTabOne(me) {
                me.formErrors.unselectedTemplateError = ('' === '' + me.formmodel.templatePath && '' === '' + me.formmodel.skeletonPagePath);

                return !me.formErrors.unselectedTemplateError;
            },
            leaveTabOne() {
                if('' !== ''+this.formmodel.templatePath) {
                    $perAdminApp.getApi().populateComponentDefinitionFromNode(this.formmodel.templatePath)
                }
                if('' !== ''+this.formmodel.skeletonPagePath) {
                    const skeletonPage = this.skeletonPages.find(bp => {
                        return bp.path == this.formmodel.skeletonPagePath
                    })
                    $perAdminApp.getApi().populateComponentDefinitionFromNode(skeletonPage.templatePath)
                }

                return this.validateTabOne(this);
            },
            leaveTabTwo() {
                const isValid = this.$refs.nameTab.validate()
                if (isValid) {
                    this.isLastStep = true
                }
                return isValid
            },
            onThumbnail() {
                let imgBase64 = null
                var iframeHtml = this.$refs.iframeOne.contentWindow
                const iframeBody = iframeHtml.document.getElementsByTagName('body')[0]
                const iframeScrollY = iframeHtml.document.documentElement.scrollTop
                const iframeScrollX = iframeHtml.document.documentElement.scrollLeft
                html2canvas(iframeBody, {
                    allowTaint: true,
                    useCORS: true,
                    width: 812,
                    height: 661,
                    x: iframeScrollX,
                    y: iframeScrollY
                }).then(canvas => {
                    imgBase64 = canvas.toDataURL('image/png')
                    if (imgBase64) {
                        this.thumbnailData = imgBase64
                    } else {
                        console.log('Capture failed.')
                    }
                })
                // if (!this.$refs.iframeOne) return;
                // toPng(this.$refs.iframeOne.contentDocument.body, { cacheBust: true, })
                // .then((dataUrl) => {
                //     this.thumbnailData = dataUrl;
                // })
                // .catch((err) => {
                //     console.log(err)
                // })

            }
        }
    }
</script>

<style>
.iframe-container {
    overflow: hidden;
    padding-top: 56.25%;
    position: relative;
    box-shadow: 5px 5px 10px lightgray;
}

.iframe-container iframe {
    border: 0;
    height: 200%;
    left: 0;
    position: absolute;
    top: 0;
    width: 200%;
    transform: scale(0.5) translate(-50%, -50%);
}

.as-heading {
    font-size: 1.5rem;
}

.as-scrollable {
    max-height: 20rem;
    overflow-y: auto;
}

.as-scrollable-short {
    max-height: 9rem;
    overflow-y: auto;
}

</style>
