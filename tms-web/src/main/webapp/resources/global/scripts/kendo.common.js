function getGridOptions() {
    return {
        sortable: true,
        groupable: false,
        selectable: false,
        editable: false,
        resizable: true,
        reorderable: true,
        pageable: {refresh: true, pageSize: 20, pageSizes: [20, 50, 100, 200, 500]}
    }
}
function getDataSource(idField, readUrl, filter) {
    return new kendo.data.DataSource(getDataSourceConfig(idField, readUrl, filter))
}
function getDataSourceConfig(idField, readUrl, filter) {
    return {
        schema: {
            model: {id: idField}, data: function (response) {
                return response.list || response.body || response
            }, total: 'totalItemCount'
        }, transport: {
            parameterMap: function (options) {
                return kendo.stringify(options)
            }, read: getTransport(readUrl)
        }, pageSize: 20, serverPaging: true, serverFiltering: true, serverSorting: true, filter: filter
    }
}
function getDataListSourceConfig(idField, readUrl, filter) {
    return {
        schema: {
            model: {id: idField}, data: function (response) {
                return response.list || response.body || response
            }
        }, transport: {
            parameterMap: function (options) {
                return kendo.stringify(options)
            }, read: getTransport(readUrl)
        }, serverFiltering: false, filter: filter
    }
}
function bindHasChildren(data, childrenField) {
    $.each(data, function (i, item) {
        if (childrenField) {
            item.items = item[childrenField]
        }
        item.hasChildren = item.items.length > 0;
        if (item.hasChildren) {
            bindHasChildren(item.items, childrenField)
        }
    })
}
function getDataListSource(idField, readUrl, filter) {
    return new kendo.data.DataSource(getDataListSourceConfig(idField, readUrl, filter))
}
function getTransport(url, type, dataType, contentType) {
    return {
        contentType: contentType || 'application/json',
        dataType: dataType || 'json',
        type: type || 'POST',
        url: url
    }
}
function getHierarchicalDataSource(idField, readUrl, filter, children, hasChildren, expanded) {
    var config = {
        schema: {
            model: {id: idField}, data: function (response) {
                var list = response.list || response.body || response;
                bindHasChildren(list, children);
                return list
            }
        }, transport: {
            parameterMap: function (options) {
                return kendo.stringify(options)
            }, read: getTransport(readUrl)
        }, serverFiltering: true, filter: filter
    };
    if (expanded == undefined) {
        expanded = true
    }
    config.schema.model.expanded = expanded;
    if (children)config.schema.model.children = children;
    if (hasChildren)config.schema.model.hasChildren = hasChildren;
    return new kendo.data.HierarchicalDataSource(config)
}
function getTreeListDataSource(idField, readUrl, parentField, expanded) {
    if (expanded == undefined) {
        expanded = true
    }
    var datasource = {
        schema: {
            model: {
                id: idField,
                fields: {parentId: {field: parentField, nullable: true}},
                expanded: expanded
            }, data: function (response) {
                return response.list || response.body || response
            }
        }, transport: {
            parameterMap: function (options) {
                return kendo.stringify(options)
            }, read: getTransport(readUrl)
        }, serverFiltering: true, serverSorting: true
    };
    return new kendo.data.TreeListDataSource(datasource)
}
function getComboDatasource(url) {
    return new kendo.data.DataSource({
        serverFiltering: false,
        transport: {read: {dataType: "json", url: contextPath + url}}
    })
}