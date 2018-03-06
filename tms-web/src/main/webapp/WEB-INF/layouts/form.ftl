<#macro Text id name=id label=id ngChange="" ngModel="" type="text" required=false maxLength="" minLength="" readonly=false pattern="" placeholder="" equalTo="">
<label class="control-label col-sm-4" for="${id}">${label}
    <#if required><span class="required" aria-required="true"> * </span></#if>
</label>
<div class="col-sm-8">
    <input type="${type}" class="k-textbox"<#if ngModel?has_content>ng-model="${ngModel}" </#if> <#if ngChange?has_content>
           ng-change="${ngChange}" </#if> <#if required>required="${required}"</#if>
           <#if readonly>readonly="${readonly}"</#if> <#if maxLength?has_content>maxlength="${maxLength}" </#if>
           <#if minLength?has_content>minLength="${minLength}" </#if>
           <#if pattern?has_content>pattern="${pattern}"</#if> id="${id}" name="${name}" <#if placeholder?has_content>
           placeholder="${placeholder}"</#if>/>
</div>
</#macro>

<#macro Textarea ngModel id name=id label=id ngChange="" rows=2 cols=60 required=false maxLength="" minLength="" readonly=false pattern="" placeholder="" >
<label class="control-label col-sm-4" for="${id}">${label}
    <#if required><span class="required" aria-required="true"> * </span></#if>
</label>
<div class="col-sm-8">
    <textarea type="text" class="k-textbox" <#if ngModel?has_content>ng-model="${ngModel}" </#if> rows="${rows}"
              cols="${cols}" <#if ngChange?has_content> ng-change="${ngChange}" </#if>
              <#if required>required="${required}"</#if> <#if readonly>readonly="${readonly}"</#if>
              <#if maxLength?has_content>maxlength="${maxLength}" </#if>
              <#if minLength?has_content>minLength="${minLength}" </#if>
              <#if pattern?has_content>pattern="${pattern}"</#if> id="${id}"
              name="${name}" <#if placeholder?has_content> placeholder="${placeholder}"</#if>></textarea>
</div>
</#macro>
<#macro MaskText id mask name=id label=id ngChange="" ngModel="" type="text" required=false maxLength="" minLength="" readonly=false pattern="" placeholder="" equalTo="">
<label class="control-label col-sm-4" for="${id}">${label}
    <#if required><span class="required" aria-required="true"> * </span></#if>
</label>
<div class="col-sm-8">
    <input type="${type}" kendo-masked-text-box k-mask="'${mask}'" class="k-textbox" <#if ngModel?has_content>ng-model="${ngModel}" </#if> <#if ngChange?has_content>
           ng-change="${ngChange}" </#if> <#if required>required="${required}"</#if>
           <#if readonly>readonly="${readonly}"</#if> <#if maxLength?has_content>maxlength="${maxLength}" </#if>
           <#if minLength?has_content>minLength="${minLength}" </#if>
           <#if pattern?has_content>pattern="${pattern}"</#if> id="${id}" name="${name}" <#if placeholder?has_content>
           placeholder="${placeholder}"</#if>/>
</div>
</#macro>
<#macro CheckBox id label=id name=id ngClick="" ngModel="" disabled=false>
<label class="control-label col-sm-4" for="${id}">${label}</label>
<div class="col-sm-8 form-control-static">
    <input type="checkbox" id="${id}" name="${name}" <#if ngModel?has_content>ng-model="${ngModel}" </#if> <#if disabled>disabled="${disabled}"</#if>
           <#if ngClick?has_content>ng-click="${ngClick}"</#if> checked/>
</div>
</#macro>

<#macro NumberBox id ngModel label=id name=id required=false readonly=false ngChange="" min="" max="" format="" decimals=2 placeholder="">
<label class="control-label col-sm-4" for="${id}">${label}
    <#if required><span class="required" aria-required="true"> * </span></#if>
</label>
<div class="col-sm-8">
    <input type="number" kendo-numeric-text-box <#if ngModel?has_content>ng-model="${ngModel}" </#if>
        <#if format?has_content>k-format="'${format}'"</#if> k-decimals="'${decimals}'"<#if ngChange?has_content>
           ng-change="${ngChange}" </#if> <#if required>required="${required}"</#if>
           <#if readonly>readonly="${readonly}"</#if> <#if max?has_content>max="${max}" </#if>
           <#if min?has_content>min="${min}" </#if> id="${id}" name="${name}" <#if placeholder?has_content>
           placeholder="'${placeholder}'"</#if>/>
</div>
</#macro>

<#macro DatePicker id ngModel="" label=id name=id ngChange="" format="yyyy-MM-dd" culture="zh-CN" required=false readonly=false placeholder="">
<label class="control-label col-sm-4" for="${id}">${label}
    <#if required><span class="required" aria-required="true"> * </span></#if>
</label>
<div class="col-sm-8">
    <input type="date" kendo-date-picker <#if ngModel?has_content>ng-model="${ngModel}" </#if> k-format="'${format}'"
           k-culture="'${culture}'"<#if ngChange?has_content> ng-change="${ngChange}" </#if>
           <#if required>required="${required}"</#if> <#if readonly>readonly="${readonly}"</#if> id="${id}"
           name="${name}" <#if placeholder?has_content> placeholder="${placeholder}"</#if>/>
</div>
</#macro>

<#macro DateTimePicker id label=id name=id ngChange="" ngModel="" format="yyyy-MM-dd HH:mm:ss" culture="zh-CN" required=false readonly=false placeholder="">
<label class="control-label col-sm-4" for="${id}">${label}
    <#if required><span class="required" aria-required="true"> * </span></#if>
</label>
<div class="col-sm-8">
    <input type="datetime" kendo-date-time-picker <#if ngModel?has_content>ng-model="${ngModel}" </#if>  k-format="'${format}'"
           k-culture="'${culture}'"<#if ngChange?has_content> ng-change="${ngChange}" </#if>
           <#if required>required="${required}"</#if> <#if readonly>readonly="${readonly}"</#if> id="${id}"
           name="${name}" <#if placeholder?has_content> placeholder="${placeholder}"</#if>/>
</div>
</#macro>

<#macro Dropdown id listItem name=id label=id ngModel="" ngName="" ngChange="" options="" textField="text" valueField="value" required=false readonly=false placeholder="" dataBound="">
<label class="control-label col-sm-4" for="${id}">${label}
    <#if required><span class="required" aria-required="true"> * </span></#if>
</label>
<div class="col-sm-8">
    <select id="${id}" name="${name}" <#if ngModel?has_content>ng-model="${ngModel}" </#if><#if ngName?has_content>ng-name="${ngName}" </#if>  kendo-drop-down-list
            <#if options?has_content>k-options="${options}"</#if>
            <#if dataBound?has_content>k-data-bound="${dataBound}"</#if>
            <#if ngChange?has_content>ng-change="${ngChange}" </#if><#if required>
            required="${required}" </#if> <#if readonly>
            readonly="${readonly}"</#if> <#if placeholder?has_content&&!listItem> k-option-label="${placeholder}"</#if>>
        <#if placeholder?has_content&&listItem>
            <option value="">${placeholder}</option>
        </#if>
        <#if listItem>
            <#list listItem as item>
                <option <#if valueField?has_content>value="${item[valueField]}"
                        <#else >value="${item}" </#if>><#if textField?has_content>${item[textField]}<#else >${item}</#if></option>
            </#list>
        </#if>
    </select>
</div>
</#macro>

<#macro ComboBox id listItem name=id label=id ngChange="" ngModel="" ngName="" options="" textField="text" valueField="value" required=false readonly=false placeholder="" dataBound="">
<label class="control-label col-sm-4" for="${id}">${label}
    <#if required><span class="required" aria-required="true"> * </span></#if>
</label>
<div class="col-sm-8">
    <select id="${id}" name="${name}" <#if ngModel?has_content>ng-model="${ngModel}"</#if> <#if ngName?has_content>ng-name="${ngName}" </#if> kendo-combo-box
            <#if options?has_content>k-options="${options}"</#if>
            <#if dataBound?has_content>k-data-bound="${dataBound}"</#if>
            <#if ngChange?has_content>ng-change="${ngChange}" </#if><#if required>
            required="${required}" </#if> <#if readonly>
            readonly="${readonly}"</#if> <#if placeholder?has_content&&!listItem> placeholder="${placeholder}"</#if>>
        <#if listItem>
            <#list listItem as item>
                <option <#if valueField?has_content>value="${item[valueField]}"
                        <#else >value="${item}" </#if>>${item[textField]}</option>
            </#list>
        </#if>
    </select>
</div>
</#macro>

<#macro MultiSelect ngModel id listItem name=id label=id ngChange="" options="" textField="text" valueField="value" required=false readonly=false placeholder="" dataBound="">
<label class="control-label col-sm-4" for="${id}">${label}
    <#if required><span class="required" aria-required="true"> * </span></#if>
</label>
<div class="col-sm-8">
    <select id="${id}" name="${name}" <#if ngModel?has_content>ng-model="${ngModel}" </#if>  multiple="multiple" kendo-multi-select
            <#if options?has_content>k-options="${options}"</#if>
            <#if dataBound?has_content>k-data-bound="${dataBound}"</#if>
            <#if ngChange?has_content>ng-change="${ngChange}" </#if><#if required>
            required="${required}" </#if> <#if readonly>
            readonly="${readonly}"</#if> <#if placeholder?has_content&&!listItem> placeholder="${placeholder}"</#if>>
        <#if listItem>
            <#list listItem as item>
                <option <#if valueField?has_content>value="${item[valueField]}"
                        <#else >value="${item}" </#if>>${item[textField]}</option>
            </#list>
        </#if>
    </select>
</div>
</#macro>

<#macro DropDownTreeView ngModel id listItem name=id label=id ngChange="" options="" required=false readonly=false placeholder="">
<label class="control-label col-sm-4" for="${id}">${label}
    <#if required><span class="required" aria-required="true"> * </span></#if>
</label>
<div class="col-sm-8">
    <input id="${id}" name="${name}" <#if ngModel?has_content>ng-model="${ngModel}" </#if>  kendo-drop-down-tree-view
           <#if options?has_content>k-options="${options}"</#if><#if ngChange?has_content>ng-change="${ngChange}" </#if><#if required>
           required="${required}" </#if> <#if readonly>
           readonly="${readonly}"</#if> <#if placeholder?has_content&&!listItem> placeholder="${placeholder}"</#if>/>
</div>
</#macro>

<#macro DistrictSelect id province="" city="" area="" street="" name=id label=id ngChange="" ngModel="" type="text" required=false maxLength="" minLength="" readonly=false pattern="" placeholder="" equalTo="">
<label class="control-label col-sm-4" for="${id}_typeahead">${label}
    <#if required><span class="required" aria-required="true"> * </span></#if>
</label>
<div class="col-sm-8">
    <input type="${type}" class="k-textbox" kendo-district-select <#if ngModel?has_content>ng-model="${ngModel}" </#if> <#if ngChange?has_content>
           ng-change="${ngChange}" </#if> <#if required>required="${required}"</#if> k-province="'${province}'" k-city="'${city}'"
           k-area="'${area}'" k-street="'${street}'"
           <#if readonly>readonly="${readonly}"</#if>
           id="${id}" name="${name}" <#if placeholder?has_content>
           placeholder="${placeholder}"</#if>/>
</div>
</#macro>