/**
 * jQuery Autocomplete plugin 1.0
 * 
 * Created by duanyunhu 2014-12-05
 * 
 * @author duanyunhu
 * @param options
 * 参数使用说明:
 * 	keyWordName : 关键字提交参数的key值
 * 	source: 当不需要请求后台数据时需要显示的数据
 * 	url: 自动补全请求的数据请求地址
 * 	extraParams : 额外的其他固定参数
 * 	onFocursAutoComplete : 文本框聚焦自动补全
 * 	minLength :  文本框输入最小字符长度进行自动补全请求
 * 	selected :  选中选项回调函数
 * 	formatItem : 显示数据回调,此函数需要返回显示每个item的字符串
 * 	dataParse : 远程调用返回数据预处理,此函数需要返回json数组
 */
(function($) {
    $.fn.autocomplete = function(options) {

    	var searching = false;
    	var self = $(this);
        var defaults = {
            keyWordName: 'keyWord',
            source: [],
            url:"",
            extraParams : {},
            onFocursAutoComplete : false,
            minLength : 2,
            selected : function(item){
                self.val(item.name);
            },
            formatItem : function(item){
                return "<p>"+item.name+"</p>";
            },
            dataParse:null
        };

        var opts = $.extend(defaults, options);
        
        self.attr("autocomplete", "off");
        
        var autoCmtDiv = $('body .autocmtDiv');
        if(autoCmtDiv.length<=0){
        	autoCmtDiv = $('<div class="autocmtDiv"><ul class="autocmtUl"></ul></div>').appendTo($('body'));
        }
        var ulElement = autoCmtDiv.find('.autocmtUl');
        ulElement.empty();
        
        var needHide = true;
        self.unbind('blur.autocomplete').bind('blur.autocomplete',function(){
            if(needHide){
            	autoCmtDiv.hide();
            }
        });

        autoCmtDiv.unbind('mouseleave.autocomplete').bind('mouseleave.autocomplete',function(){
            needHide = true;
            self.focus();
        }).unbind('mouseenter.autocomplete').bind('mouseenter.autocomplete',function(){
            needHide = false;
        });

        var display = function(data){
        	searching = false;
            ulElement.empty();

            if(!data || data.length==0){
                return;
            }
            var inputHeight = self.outerHeight()||0;
            var inputWidth = self.innerWidth()||0;
            autoCmtDiv.css({
            	position: 'absolute',
            	left : self.offset().left,
            	top : self.offset().top+inputHeight,
            	width : inputWidth
            });
            var p = '';
            for(var i=0;i<data.length;i++){
                var item = data[i];
                p = opts.formatItem(item);

                var element = $('<li>'+p+'</li>');
                element.data('item',item);
                element.bind('click.autocomplete',function(){
                    var itemData = $(this).data('item');
                    opts.selected(itemData);
                    autoCmtDiv.hide();
                });
                element.bind('mouseover.autocomplete',function(){
                    ulElement.find('li').removeClass('hover');
                    $(this).addClass('hover');
                });
                ulElement.append(element);
            }
            
            autoCmtDiv.show();
        };

        function pressKey(event){
            if(!autoCmtDiv || autoCmtDiv.css('display')=='none'){
                return;
            }
            var keyCode = event.keyCode;
            if(keyCode==40){ //down

                var hover = ulElement.find('li.hover');

                var next;
                if(hover && hover.length>0){
                    next = $(hover).next('li');
                    if(next.length==0){
                    	next = ulElement.find('li:first');
                    }
                }else{
                    next = ulElement.find('li:first');
                }
                ulElement.find("li.hover").removeClass('hover');
                if(next){
                    $(next).addClass('hover');
                }
            }else if(keyCode==38){ //up
                var hover = ulElement.find("li.hover");
                var prev;
                if(hover && hover.length>0){
                	prev = $(hover).prev('li');
                	if(prev.length==0){
                		prev = ulElement.find('li:last');
                	}
                }else{
                	prev = ulElement.find('li:first');
                }
                ulElement.find('li.hover').removeClass('hover');
                if(prev){
                    $(prev).addClass('hover');
                }
            }else if(keyCode==13){
                var hover = ulElement.find('li.hover');
                if(hover && hover.length>0){
                    hover.trigger('click.autocomplete');
                }
            }
        }

        var dataFilter = function(data){
        	var showData = data;
        	if(opts.dataParse){
        		showData = opts.dataParse(data);
        	}
            display(showData);
        };
        
        var doSearch = function(){
        	if(searching){
        		return;
        	}
        	var source = opts.source;
            var url = opts.url;
        	if(!url||url.length==0){
        		display(source);
        		return;
        	}
        	searching = true;
            var keyWord = self.val();
            var param = {};
            param[opts.keyWordName] = keyWord;
            var params = $.extend(param, opts.extraParams);
            
            $.ajax({
                type : 'POST',
                url : opts.url,
                dataType : 'json',
                cache : false,
                data : params,
                success : function(data){
                	dataFilter(data);
                },
                error : function(error){
                	searching = false;
                }
            });
        };

        self.unbind('keyup.autocomplete').bind('keyup.autocomplete',function(event){
            var keyCode = event.keyCode;
            if(keyCode==40 || keyCode==38 || keyCode==13){
                if(autoCmtDiv){
                    //$(this).focus();
                    pressKey(event);
                    return;
                }
            }
            autoCmtDiv.hide();
            var keyWord = self.val();
            if(keyWord.length<opts.minLength){
                return;
            }
            doSearch();
        });
        if(opts.onFocursAutoComplete){
        	self.unbind('click.autocomplete').bind('click.autocomplete',function(){
                doSearch();
        	});
        }
        return self;
    };
})(jQuery);