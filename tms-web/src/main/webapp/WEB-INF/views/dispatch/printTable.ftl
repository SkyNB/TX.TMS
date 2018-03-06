<style type="text/css">

    /*    .bigFont {
            font-size: 11pt;
            font-weight: bold;
        }*/

    table {
        border: solid 1px #000;
        border-collapse: collapse;
        width: 100%;
    }

    tr {
        border: solid 1px #000;
    }

    td, th {
        border: solid 1px #000;
        padding: 1px;
        text-align: center;


    }

    /*th {
        background: #efefef;
    }*/

    table.noborder {
        border: none;
    }

    table.noborder tr {
        border: none;
    }

    table.noborder td, table.noborder th {
        border: none;
        text-align: left;
    }

</style>
<#--<#list printDispatch as print>-->
<div class="chapter" style="page-break-after:always">
    <table style="font-size: 12px;">
        <thead>

        <tr>
            <th>分公司</th>
            <th>项目名称</th>
            <th>客户单号</th>
            <th>收件人</th>
            <th>打包箱数</th>
            <th>应收体积</th>
            <th>重量</th>
            <th>收货目的地</th>
        </tr>

        </thead>
        <tbody>
        <#assign count = 0>
        <#assign totalWeight = 0>
        <#assign totalPackageQty = 0>
        <#assign totalVolume = 0>
        <#list dispatchItemDtoList as print>

        <#assign count = count+1>
        <#assign totalPackageQty = totalPackageQty +print.packageQuantity>
        <#assign totalWeight = totalWeight +print.weight>
         <#assign totalVolume = totalVolume +print.volume>
        <tr>

            <td class="bigFont">${print.branchName}</td>
            <td class="bigFont">${print.customerName}</td>
            <td class="bigFont">${print.customerOrderNo}</td>
            <td class="bigFont">${print.deliveryContacts}</td>
            <td class="bigFont">${print.packageQuantity}</td>
            <td class="bigFont">${print.volume?string("0.######")}</td>
            <td class="bigFont">${print.weight?string("0.####")}</td>
            <td class="bigFont">${print.destinationName}+${print.deliveryAddress}</td>
        </tr>
        </#list>
        </tbody>
        <tfoot>
        <tr>
            <th>合计</th>
            <td class="bigFont"></td>
            <td class="bigFont"></td>
            <td class="bigFont"></td>
            <td class="bigFont">${totalPackageQty}</td>
            <td class="bigFont">${totalVolume?string("0.######")}</td>
            <td class="bigFont">${totalWeight?string("0.####")}</td>
            <td class="bigFont"></td>
        </tr>
        <tr>
            <th>备注</th>
            <td colspan="10" class="bigFont"></td>

        </tr>
        </tfoot>
    </table>
    <table class="noborder" style="margin-top: 1em">
        <tr>
            <td>制单人：${dispatch.createUserName}</td>
            <td>签收人：</td>

        </tr>
        <tr>
            <td>制单日期：${.now?string("yyyy年MM月dd日")}</td>
            <td>签收日期：</td>
        </tr>
    </table>
    <p style="margin-top: 1em">
    </p>

</div>
<#--</#list>-->



