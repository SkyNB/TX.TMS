var districtData = null;
(function ($) {
    var kendo = window.kendo, ui = kendo.ui;
    var val = "";
    var DistrictSelect = kendo.ui.Widget.extend({
        _uid: null,
        _v: null,
        input: null,
        data: [],
        state: "empty",
        dataBlood: null,
        init: function (element, options) {
            var that = this;
            if (districtData == null) {
                $.ajax({
                    url: contextPath + "/district/getHierarchical",
                    type: "POST",
                    contentType: "application/json",
                    async: false
                }).done(function (result) {
                    var orgData = result.body;
                    for (var i = 0; i < orgData.length; i++) {
                        orgData[i].fullName = orgData[i].name;
                        orgData[i].fullJP = orgData[i].namePinyinHead;
                        that.data.push(orgData[i]);
                        if (orgData[i].children && orgData[i].children.length > 0) {
                            that.concatName(orgData[i].children, orgData[i].name, orgData[i].namePinyinHead)
                        }
                    }
                    districtData = that.data
                })
            }
            that.data = districtData;
            that.dataBlood = new Bloodhound({
                local: that.data,
                identify: function (obj) {
                    if(obj) return obj.code;
                },
                queryTokenizer: Bloodhound.tokenizers.whitespace,
                datumTokenizer: Bloodhound.tokenizers.obj.whitespace("fullName", "fullJP", "name", "code", "namePinyinAll", 'namePinyinHead')
            });
            kendo.ui.Widget.fn.init.call(this, element, options);
            that.input = $("<input id='" + $(element).attr('id') + "_typeahead'>");
            that.input.addClass("k-textbox").appendTo($(element).parent());
            $(element).appendTo($(element).parent());
            $(element).attr("type", "hidden");
            that.input.typeahead({highlight: true, minLength: 0}, {
                display: 'fullName',
                val: "code",
                limit: 10,
                source: that.dataBlood
            }).bind('typeahead:select', function (ev, selectedItem) {
                that.onSelect(selectedItem)
            }).bind('typeahead:autocomplete', function (ev, selectedItem) {
                that.onSelect(selectedItem)
            }).bind('typeahead:open', function (ev) {
            }).bind('blur', function (ev) {
                $(this).trigger("typeahead:change")
            }).bind('focus', function (ev) {
            }).bind('typeahead:change', function (ev, value) {
                if (value === "") {
                    $(element).val("");
                    that.input.val("")
                }
                var values = that.dataBlood.get($(element).val());
                if (values.length == 1) {
                    that.input.val(values[0].fullName)
                }
            })
        },
        concatName: function (children, parentName, parentPH) {
            if(children&&children.length>0){
                for (var i = 0; i < children.length; i++) {
                    children[i].fullName = parentName + children[i].name;
                    children[i].fullJP = parentPH + children[i].namePinyinHead;
                    this.data.push(children[i]);
                    if (children[i].children && children[i].children.length > 0) {
                        this.concatName(children[i].children, children[i].fullName, children[i].fullJP)
                    }
                }
            }
        },
        onSelect: function (selectedItem) {
            var that = this;
            $(that.element).val(selectedItem.code).trigger('change');
            that.bindArea(selectedItem.code);
            if (selectedItem.code.length === 9) {
                that.state = "street"
            } else {
                if (selectedItem.code.substr(2, 2) == '00') {
                    that.state = "province"
                } else if (selectedItem.code.substr(4, 2) == '00') {
                    that.state = "city"
                } else {
                    $.ajax({
                        url: contextPath + "/district/getChildren/" + selectedItem.code,
                        type: "POST",
                        contentType: "application/json"
                    }).done(function (result) {
                        $.each(result.body, function (i, item) {
                            item.fullName = selectedItem.fullName + item.name
                        });
                        that.dataBlood.add(result.body);
                        that.open()
                    })
                }
            }
        },
        open: function () {
            var ev = $.Event("keydown");
            ev.keyCode = ev.which = 32;
            this.input.typeahead('val', this.input.typeahead('val').trim() + " ").trigger(ev)
        },
        options: {name: "DistrictSelect"},
        getItem: function () {
            return this.dataBlood.get($(this.element).val(v))
        },
        bindArea: function (v) {
            var that = this;
            if (v.length !== 6 && v.length !== 9)return;
            // $(that.options.province).val("").trigger("change");
            // $(that.options.city).val("").trigger("change");
            // $(that.options.area).val("").trigger("change");
            // $(that.options.street).val("").trigger("change");
            if (v.substr(0, 2).indexOf("00") < 0) {
                var province = this.dataBlood.get(v.substr(0, 2) + "0000")[0];
                if (province)$(that.options.province).val(province.name).trigger("change")
            }
            if (v.substr(0, 4).lastIndexOf("00") != 0) {
                var city = this.dataBlood.get(v.substr(0, 4) + "00")[0];
                if (city)$(that.options.city).val(city.name).trigger("change")
            }
            if (v.substr(0, 6).lastIndexOf("00") != 0) {
                var area = this.dataBlood.get(v.substr(0, 6))[0];
                if (area)$(that.options.area).val(area.name).trigger("change")
            }
            if (v.length === 9) {
                var street = this.dataBlood.get(v)[0];
                if (street)$(that.options.street).val(street.name).trigger("change")
            }
        },
        value: function (v) {
            var that = this;
            if (v == null || v == undefined) {
                return $(that.element).val();
            } else {
                if (v == "") {
                    $(that.element).val("").trigger('change');
                    that.input.val("");
                    that.state = "empty";
                } else {
                    $(that.element).val(v).trigger('change');
                    if (v.length === 9) {
                        var parent = that.dataBlood.get(v.substr(0, 6))[0] || that.dataBlood.get(v.substr(0, 4) + "00")[0];
                        var parentName = parent.fullName;
                        $.ajax({
                            url: contextPath + "/district/getChildren/" + v.substr(0, 6),
                            type: "POST",
                            contentType: "application/json",
                            async: false
                        }).done(function (result) {
                            $.each(result.body, function (i, item) {
                                item.fullName = parentName + item.name
                            });
                            that.dataBlood.add(result.body)
                        })
                    }
                    if (that.dataBlood.get(v).length === 1) {
                        that.input.val(that.dataBlood.get(v)[0].fullName)
                    }
                }
                that.bindArea(v);
            }
        },
        selectItem: function () {
            var that = this;
            var district = {};
            district.code = $(that.element).val();
            district.provinceCode = $(that.element).val().substr(0, 2) + "0000";
            district.cityCode = $(that.element).val().substr(0, 4) + "00";
            district.areaCode = $(that.element).val().substr(0, 6);
            return district;
        }
    });
    ui.plugin(DistrictSelect)
})(jQuery);