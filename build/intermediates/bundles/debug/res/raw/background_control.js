var path;
var startX;
var startY;
var layoutHeight;
var layoutWidth;
var pages;
var moveTemp = 0;
var tempX = 0;
var currentPage = 1;
var currentPercent;// actual percent
var tempPosition = 0;
var chapterSize;
var size;
var bookSize;
var chapterSize;
var chapterTotleNum;
var brightness;
var move = 0;
var leftPosition = 0;
var preventMove = 0;
var toggleMenu = 1;
var bookIdentifier;
var bookmarkIndexArray = new Array();
var injectBackJS = "unInjectBackJS";
var inReadingPage = "inReadingPage";
var androidLongtouch = 0;
var multipleTouch = 0;
var bookmark = new Array();
var androidVersion = 0;

var padding;
var cWPadding;
var cHPadding;
var currentPageHeight;

var subchapterId;
var cover;
var title;
var percentTooltipPercent;
var stop;

var isMoving;

var serializedHighlights = decodeURIComponent(window.location.search.slice(window.location.search.indexOf("=") + 1));
var highlighter;

/** Inject span tag */
// ini fungsinya buat apa yes .. ???
// injek itu --> suntik :|
function injectSpanTag(pArray) {
    Android.log("injectSpanTag(" + pArray + ")");
    // melakukan looping pada array ..
    for ( var j = 0; j < pArray.length; j++) {
        var pTag = pArray[j][0]; // tag pnya
        var pIndex = pArray[j][1]; // paragraph index
        // tiap afdbooktag dipisahkan oleh titik dan koma ternyatah .. sodara
        // sodara .. :)
        // jika afdbooktag pertama undefined atau id dari afdbooktag pertama
        // adalah afd_span
        // bikin tag pertama adalah afd_booktag dengan id=afd_span
        if (typeof ($(pTag).find("afdbooktag")[0]) == 'undefined'
            || $($(pTag).find("afdbooktag")[0]).attr("id") != 'afd_span') {
            // text adalah tag dari htmlnya ..
            var text = $(pTag).html();
            var svgElements = getSvgTag(pTag, 'svg');
            var canvasElements = getSvgTag(pTag, 'canvas');
            text = "<afdbooktag id='afd_span'>" + text + "</afdbooktag>";
            text = text.replace(/,/g, "</afdbooktag>afd_mark<afdbooktag>")
            .replace(/afd_mark/g, ",");
            text = text.replace(/\. /g, "</afdbooktag>afd_mark<afdbooktag>")
            .replace(/afd_mark/g, ". ");
            text = text.replace(/，/g, "</afdbooktag>afd_mark<afdbooktag>")
            .replace(/afd_mark/g, "，");
            text = text.replace(/。/g, "</afdbooktag>afd_mark<afdbooktag>")
            .replace(/afd_mark/g, "。");
            $(pTag).html(text);
            // $(pTag).append(text);
            setSvgTag(pTag, svgElements, 'svg');
            setSvgTag(pTag, canvasElements, 'canvas');
        }

        var spanArray = $(pTag).find("afdbooktag");
        for ( var i = 0; i < spanArray.length; i++) {
            var left = $(spanArray[i]).offset().left;
            // var top = $(elements[i]).offset().top;
            var top = getElementTop(spanArray[i]);
            //			console.log(i + " ---> LEFT, TOP ---> " + left + " , " + top);
            if (navigator.userAgent.match(/Android/i) && androidVersion < 14) {
                // if (($afd_content.height() - 13) * (currentPage - 1) < top) {
                if (($afd_content.height()) * (currentPage - 1) < top) {
                    var sText = $(spanArray[i]).text();
                    if (i < spanArray.length - 1 && sText.length < 3)
                        sText = $(spanArray[i + 1]).text();
                    else
                        sText = $(spanArray[i]).text();
                    sText = sText.substring(0, 30) + "......";
                    var bookmarkData = new Array();
                    bookmarkData.push(chapterIndex);
                    bookmarkData.push(pIndex);
                    bookmarkData.push(i);

                    bookmarkData.push(sText);
                    bookmarkData.push($(pTag).html());
                    return bookmarkData;
                }
                if (i == spanArray.length - 1) {
                    var sText = $(spanArray[i]).text().substring(0, 30)
                    + "......";
                    var bookmarkData = new Array();
                    // chapterIndex, paragraph index, dan index dari tag
                    // <afd_booktag> dg id afd_span
                    bookmarkData.push(chapterIndex);
                    bookmarkData.push(pIndex);
                    bookmarkData.push(i);

                    bookmarkData.push(sText);
                    bookmarkData.push($(pTag).html());
                    return bookmarkData;
                }
            } else {
                if (0 < left) {
                    // alert(top+";"+$(spanArray[i]).offset().top);
                    var sText = $(spanArray[i]).text();
                    if (i < spanArray.length - 1 && sText.length < 3)
                        sText = $(spanArray[i + 1]).text();
                    else
                        sText = $(spanArray[i]).text();
                    sText = sText.substring(0, 30) + "......";
                    var bookmarkData = new Array();
                    bookmarkData.push(chapterIndex);
                    bookmarkData.push(pIndex);
                    bookmarkData.push(i);

                    bookmarkData.push(sText);
                    bookmarkData.push($(pTag).html());
                    return bookmarkData;
                }
                if (j < pArray.length - 1)
                    continue;
                if (i == spanArray.length - 1) {
                    var sText = $(spanArray[i]).text().substring(0, 30)
                    + "......";
                    var bookmarkData = new Array();
                    bookmarkData.push(chapterIndex);
                    bookmarkData.push(pIndex);
                    bookmarkData.push(i);

                    bookmarkData.push(sText);
                    bookmarkData.push($(pTag).html());
                    return bookmarkData;
                }
            }
        }
    }
}

/** store data with localStorage */
// store bookmark data .
// jadi si bookmark ini, disimpen dalam localstorage dengan format string ..
// bookmarkIndexArray isinya cuman array yang berisi nomor paragraph dan nomor
// span <afdbookmark>
function storeData(bookmarkData) {
    if (typeof (localStorage) == 'undefined') {
        alert('Your browser does not support HTML5 localStorage. Try upgrading.');
    } else {
        // mau nyimpen data bookmark nih
        try {
            var cIndex = bookmarkData[0]; // chapter index
            var pIndex = bookmarkData[1]; // paragraph index
            var sIndex = bookmarkData[2]; // span index
            var sText = bookmarkData[3]; // 30 text pertama dari paragraph
            var pText = bookmarkData[4]; // paragraph text
            var tempArray = new Array();
            tempArray.push(pIndex);
            tempArray.push(sIndex);
            // yang disimpen di bookmark index array cuma data nomor paragraph
            // dan spannya aja
            bookmarkIndexArray.push(tempArray);
            var tempBookmark = localStorage.getItem(bookIdentifier);
            // hmmm.. jadi ini bookmark disimpan dalam bentuk string ???
            if (tempBookmark == null) {
                // kalau tidak ada databookmark, simpen data bookmark
                tempBookmark = cIndex + "afd_item" + pIndex + "afd_item"
                + sIndex + "afd_item" + sText + "afd_item" + pText;
            } else
                // kalau ada data bookmark sebelumnay ...
                // data bookmark disambung, pemisahnya afd_divide
                tempBookmark = cIndex + "afd_item" + pIndex + "afd_item"
                + sIndex + "afd_item" + sText + "afd_item" + pText
                + "afd_divide" + tempBookmark;
            // nyimpen storagenya pake setter, keynya adalah bookIdentifier
            // bookIdentifiernya apa yah ...
            localStorage.setItem(bookIdentifier, tempBookmark);
        } catch (e) {
            if (e == QUOTA_EXCEEDED_ERR) {
                alert('Quota exceeded!');
            }
        }
    }
}

/**
 * Get the current element content
 */
// getCurrentElementContens, mendapatkan elemen2 dari tag p
function getCurrentElementContent() {
    // Menemukan elemen2 halaman dengan tag p
    var elements = $afd_content.find("p");
    //	console.log("JUMLAH P :  " + elements.length); // ini nampilin total
    // paragraph 1 bab
    // jika tidak ada,
    if (elements.length == 0) {
        // bikin array, kembalikan seluruh isi kontent, lalu masukan -1 sbagai
        // index .. (index paragraph)
        var pArrayTemp = new Array();
        pArrayTemp.push(document.getElementById("afd_content"));
        pArrayTemp.push(-1);
        var pArray = new Array();
        pArray.push(pArrayTemp);
        return pArray;
    }

    // jika ada, telusuri satu satu
    // replace (/ /g,'') itu mereplace semua spasi, kalau replace (' ','') cm
    // mereplace spasi di depan
    for ( var i = 0; i < elements.length; i++) {
        // jika isinya 'undefined' atau / atau tab, lanjut
        if (elements[i].innerHTML == 'undefined'
            || $(elements[i]).text().replace(/ /g, "").replace(/ /, "").length == 0
            && i < elements.length - 1)
            continue;
        // nyari left dari tag p ke i
        var left = $(elements[i]).offset().left;
        // nyari top dari element dengan tag p ke i
        var top = getElementTop(elements[i]);
        // maksudnya apa ya ini, jika versi android kurang dari 14
        if (navigator.userAgent.match(/Android/i) && androidVersion < 14) {
            // var heightTemp = $afd_content.height() - 13; // ini titiknya ada
            // di bawah
            var heightTemp = $afd_content.height();
            if (heightTemp * (currentPage - 1) < top
                && top <= heightTemp * currentPage) {
                // bingung aku, lanjut waeee ...
                var pArrayTemp1 = new Array();
                var pArrayTemp2 = new Array();
                var pArray = new Array();
                pArrayTemp1.push(elements[i]);
                pArrayTemp1.push(i);
                pArray.push(pArrayTemp1);
                if (i + 1 < elements.length) {
                    pArrayTemp2.push(elements[i + 1]);
                    pArrayTemp2.push(i + 1);
                    pArray.push(pArrayTemp2);
                }
                return pArray;
            }
            if (top > heightTemp * currentPage) {
                if (i == 0) {
                    var pArrayTemp = new Array();
                    pArrayTemp.push(elements[i]);
                    pArrayTemp.push(i);
                    var pArray = new Array();
                    pArray.push(pArrayTemp);
                    return pArray;
                }
                if (i > 0) {
                    var pArrayTemp1 = new Array();
                    var pArrayTemp2 = new Array();
                    var pArray = new Array();
                    pArrayTemp1.push(elements[i - 1]);
                    pArrayTemp1.push(i - 1);
                    pArray.push(pArrayTemp1);
                    pArrayTemp2.push(elements[i]);
                    pArrayTemp2.push(i);
                    pArray.push(pArrayTemp2);
                    return pArray;
                }
            }
            if (top <= heightTemp * (currentPage - 1)
                && i == elements.length - 1) {
                var pArrayTemp = new Array();
                pArrayTemp.push(elements[i]);
                pArrayTemp.push(i);
                var pArray = new Array();
                pArray.push(pArrayTemp);
                return pArray;
            }
        } else {
            if (0 < left && left < layoutWidth) {
                var pArrayTemp1 = new Array();
                var pArrayTemp2 = new Array();
                var pArray = new Array();
                pArrayTemp1.push(elements[i]);
                pArrayTemp1.push(i);
                pArray.push(pArrayTemp1);
                if (i + 1 < elements.length) {
                    pArrayTemp2.push(elements[i + 1]);
                    pArrayTemp2.push(i + 1);
                    pArray.push(pArrayTemp2);
                }
                return pArray;
            }
            if (left >= layoutWidth) {
                if (i == 0) {
                    var pArrayTemp = new Array();
                    pArrayTemp.push(elements[i]);
                    pArrayTemp.push(i);
                    var pArray = new Array();
                    pArray.push(pArrayTemp);
                    return pArray;
                }
                if (i > 0) {
                    var pArrayTemp1 = new Array();
                    var pArrayTemp2 = new Array();
                    var pArray = new Array();
                    pArrayTemp1.push(elements[i - 1]);
                    pArrayTemp1.push(i - 1);
                    pArray.push(pArrayTemp1);
                    pArrayTemp2.push(elements[i]);
                    pArrayTemp2.push(i);
                    pArray.push(pArrayTemp2);
                    return pArray;
                }
            }
            if (left <= 0 && i == elements.length - 1) {
                var pArrayTemp = new Array();
                pArrayTemp.push(elements[i]);
                pArrayTemp.push(i);
                var pArray = new Array();
                pArray.push(pArrayTemp);
                return pArray;
            }
        }
    }
}

// memanggil fungsi
/**
 * Native codes invoke the method when the webview loading finished
 */
function resizePage(current_percent, pIndex, sIndex, clickBk) {
    Android.log("resizePage(" + current_percent + ", " + pIndex + ", " + sIndex
        + "," + clickBk + ")");
    setTimeout(function() {
        if (clickBk == "clickBk") {
            var element;
            if (pIndex == -1) {
                element = document.getElementById("afd_content");
            } else
                element = $afd_content.find("p")[pIndex];

            var span = $(element).find("afdbooktag")[sIndex];

            // var i = parseInt(getElementTop(span) / ($afd_content.height() -
            // 13));
            var i = parseInt(getElementTop(span) / ($afd_content.height()));
            currentPage = currentPage + i;
            leftPosition = leftPosition - i * layoutWidth;
            tempPosition = leftPosition;
            $afd_content.css({
                "left" : leftPosition + "px"
            });
        } else {
            pages = getPages();
            // jika current_percent tidak sama dengan null, mencari
            // index halaman yg dibaca
            // current page yg sedang dibaca sama dengan current percent
            // dikali jumlah halaman dibagi 10000;
            if (current_percent != 0) {
                Android.log("CURRENT PERCENT != 0 : " + current_percent);
                currentPage = parseInt((current_percent * pages) / 10000.0);
                if ((currentPage - (current_percent * pages) / 10000.0) < 0) {
                    currentPage = currentPage + 1;
                }

                leftPosition = leftPosition - (currentPage - 1) * layoutWidth;
                tempPosition = leftPosition;
                $afd_content.css({
                    "left" : leftPosition + "px"
                });
            } else
                Android.log("currentPercent null");
        }

        getReadingPercent();
    }, 100);

    saveReadingData();
    Android.hideMenu();
    
    getPageNumberData();
}

function getPage(element){
    var width = $(window).width();
    var leftOffset = element.offset().left;
    console.log("width / leftoffset " + width + " / " + leftOffset);
    return parseInt(leftOffset / width) + 1;
}

function getPageNumberData(){
  console.log("get page number data");
  var headers  =   $("#afd_content").find("h1");
  $.each(headers, function(){
      console.log("at page : " + getPage($(this)));
      console.log($(this).text());
      var subheaders = $(this).find("h2");

      $.each(subheaders, function (){
          console.log($(this).text());
          console.log("at page " + getPage($(this)));
      });
  });
  
}

function getPageData(){
  var totalWidth = $(document).scrollWidth;
  var totalPage = totalWidth / layoutWidth;
  console.log("TOTAL PAGE " + totalPage);
  Android.pageData(chapterIndex, totalPage);
}

// mendapat jumlah halaman dari chapter tertentu
/**
 * Figure out the total pages
 */
function getPages() {
    Android.log("getPages()");
    // var layoutLeft1 = $("#afd_break").position().left;
    var docWidth = document.getElementById("afd_content").scrollWidth;
    // alert(layoutLeft1+":::::"+layoutLeft+":::::"+layoutWidth);
    var pagesTemp = docWidth / layoutWidth;

    if (parseInt(pagesTemp) < pagesTemp) {
        pagesTemp = parseInt(pagesTemp) + 1;
    }
    return pagesTemp;
}

/** Open toc page */
function openPage(pageName) {
    var url = "file://" + path + "/html/" + pageName;
    window.location = url;
}
/** Add bookmark */
// add bookmark bawaan libraryinya, pelajari dulu yess...
// 
// buat add dan delete bookmark
function addBookmark() {
    Android.log("addBookmark()");
    // jika gambar bookmarknya yellow, delete bookmark, lalu ganti gambar
    // bookmarknya jadi yang ijo ..
    // deleteBookmark ? refreshBookmark ?
    /*
	 * if ($("#afd_bkImg").attr("src") == path +
	 * "/image/afd_bookmark_yellow.png") {
	 * 
	 * var lastBookmarkArray = deleteBookmark(); // isinya bookmark array dan
	 * index yang akah dihapus ..
	 * 
	 * refreshBookmark(lastBookmarkArray); // $("#afd_bkImg").attr("src", path +
	 * "/image/afd_bookmark.png"); return; }
	 */

    // jika tidak, tambahkan bookmark, lalu ubah gambar menjadi bookmark kuning
    // storeData(bookmark) ??? bookmark dari mana??
    storeData(bookmark);

// $("#afd_bkImg").attr("src", path + "/image/afd_bookmark_yellow.png");
}

/** Delete bookmark */
// kita liat ini fungsinya apah ..
// ini fungsi buat delete bookmark, lets checkit out.. :D
function deleteBookmark() {
    Android.log("deleteBookmark()");
    if (typeof (localStorage) == 'undefined') {
        alert('Your browser does not support HTML5 localStorage. Try upgrading.');
    } else {
        try {
            // bikin array buat menampung data bookmark
            var bookmarkArray = new Array();
            // dapetin data string bookmark dari localstorage, inget
            // databookmark itu terbentuk dari
            // string yg dipisahkan oleh string 'afd_divide'
            var tempBookmark = localStorage.getItem(bookIdentifier);
            // kalau bookmark null, kembali, sisa fungsi tidak dieksekusi
            if (tempBookmark == null)
                return;

            // bikin bookomark array, dengan menyeplit bookmark dgn key
            // 'afd_divide'
            var tempBookmarkArray = tempBookmark.split("afd_divide");
            var num = tempBookmarkArray.length; // panjang bookmark

            // dapetin bookmark array
            // jadi variabel bookmark array nanti isinya adalah array yang
            // terdiri dari data bookmark
            // data bookmark -> chapter index, paragraph index, span index, 30
            // karakter pertama dari paragraph, dan konten dari paragraphnya
            for ( var i = 0; i < num; i++) {
                var bookmarkData = tempBookmarkArray[i].split("afd_item");
                bookmarkArray.push(bookmarkData);
            }

            // liat ini fungsinya buat apa.
            // perulangan dari bookmarkArray .. mau menghapus bookmarkIndexArray
            // ...
            for ( var i = 0; i < bookmarkArray.length; i++) {
                // jika chapterIndex == bookmarkArray[i][0], perulangannya tidak
                // sampai selesai loh
                if (chapterIndex == bookmarkArray[i][0]) {
                    // ambil data paragraph index, span index, dan elemennya ..
                    var pIndex = bookmarkArray[i][1];
                    var sIndex = bookmarkArray[i][2];
                    var element;
                    if (pIndex == -1) {
                        element = document.getElementById("afd_content");
                    } else
                        element = $afd_content.find("p")[pIndex];
                    var span = $(element).find("afdbooktag")[sIndex];

                    // looping dari bookmarkindexArray / ingat obookmarkindex
                    // array isinya cuman array yg bersisi paragraph index dan
                    // span index
                    // ini mau menghapus menghapus bookmarkIndexArray, setelah
                    // ketemu pengulangan dihentikan
                    for ( var j = 0; j < bookmarkIndexArray.length; j++) {
                        if (pIndex == bookmarkIndexArray[j][0]
                            && sIndex == bookmarkIndexArray[j][1]) {
                            // menghapus array ke j, sejumlah 1...
                            bookmarkIndexArray.splice(j, 1);
                            break;
                        }
                    }
                    // last bookmark array adalah bookmarkarrray ( berisi array
                    // dari paragraph index & span index ) stelah dihapus
                    var lastBookmarkArray = new Array();
                    // mengepush bookmarkArray, tidak ada yg dihapus dari
                    // bookmark array
                    lastBookmarkArray.push(bookmarkArray);
                    // ini ngepush index dari index bookmark array
                    lastBookmarkArray.push(i);
                    // refreshBookmark(bookmarkArray,i);
                    // jadi lastbookmark array isinya bookmark array dan index
                    // yang akan dihapus???
                    return lastBookmarkArray;

                }
            }
        } catch (e) {
            if (e == QUOTA_EXCEEDED_ERR) {
                alert('Quota exceeded!');
            }
        }
    }

}

/** refresh bookmark data */

// jadi refreshBookmark itu adalah ...
// mendapatkan data bookmarkArray dan index dari yang akan dihapus ...
// join join bookmarkArray sehingga jadi string yang ada afd_item itu ..
// hapus bookmarkArray dengan index tertentu
// lalu set ke localstorage lagi ..
function refreshBookmark(lastBookmarkArray) {
    // lastBookmarArray ini isinya daftar bookmarmark array sama index yg mau
    // dihapus
    Android.log("refreshBookmark(" + lastBookmarkArray + ")");
    var deleteBKIndex = lastBookmarkArray[1]; // index yg mau di delete
    // ini bookmark array, tiap item isinya index chapter, paragraph chapter,
    // span index, 30 karakter pertama dari paragraph,
    var bookmarkArray = lastBookmarkArray[0];
    // ngehapus index ke deleteBKIndex..
    bookmarkArray.splice(deleteBKIndex, 1);
    var tempBookmarkArray = new Array();
    var tempBookmark;
    // kalau tidak ada bookmark, hapus databookmark dari localstorage
    if (bookmarkArray.length == 0) {
        localStorage.removeItem(bookIdentifier);
    } else {
        // kalau ada bookmark,a lalukakn perulangan ..
        // join tiap item dari tiap item ..
        // nanti hasip akhirnya
        // 1afd_item2afd_item0afd_itemPotonganParagraphafd_item
        for ( var i = 0; i < bookmarkArray.length; i++) {
            var bookmarkData = bookmarkArray[i];
            var temp = bookmarkData.join("afd_item");
            tempBookmarkArray.push(temp);
        }
        tempBookmark = tempBookmarkArray.join("afd_divide");

        if (typeof (localStorage) == 'undefined') {
            alert('Your browser does not support HTML5 localStorage. Try upgrading.');
        } else {
            try {
                // simpen lagi di localstorage
                localStorage.setItem(bookIdentifier, tempBookmark);
            } catch (e) {
                if (e == QUOTA_EXCEEDED_ERR) {
                    alert('Quota exceeded!');
                }
            }
        }
    }
}
/** Set bookmark image */
// ngeset bookmark, mari kita cari tahu .. (--,)9
// ternyata fungsinya buat nentuain bookmark data... dan mencari bookmark data
// itu udah ada di daftar bookmark atau belum ...
function setBookmarkImg() {
    Android.log("setBookmarkImg()");
    // jika menu atas && scale panel atas tidak tampil .. maka ..
    // oh ini kan harusnya dieksekusi saat panel menu tidak ada..
    // if ($afd_menu.css("display") == "none" && $afd_scale_panel.css("display")
    // == "none") {
    // 1. ngeset bookmark image, jadi yg ijo itu
    // $("#afd_bkImg").attr("src", path + "/image/afd_bookmark.png");
    // mendaptakan currentElementContent
    // berisi 2 paragraf pertama dari atas, atau dari halaman sebelumnya ..
    var pArray = getCurrentElementContent();

    // console.log("start");
    // console.log("pArray : " + pArray);
    // console.log(pArray[0][1] + " --> " + pArray[0][0].innerHTML);
    // console.log(pArray[1][1] + " --> " + pArray[1][0].innerHTML);
    // console.log("LENGTH " + pArray.length);
    // for(i=0;i<pArray.length;i++){
    // console.log(i + " ---> " + pArray[i]);
    // console.log("ISI " + pArray[i][0].innerHTML);
    // }

    // console.log(pArray[0][0].innerHTML);
    // console.log(pArray[0][1]);
    // console.log(pArray[1][0].innerHTML);
    // console.log(pArray[1][1]);
    // console.log("stop");
    // mendapatkan bookmark dat dengan inject span tag
    var bookmarkData = injectSpanTag(pArray); // memasukan tag span
    // console.log("BOOKMARK DATA ----> " + bookmarkData);
    // bookmark data itu isinya ..
    // currentChapter, paragraph (dari 0), span dari tag afd_bookmark, 30
    // karakterp pertama dari nomor paragragh, dan juga isi dari paragraphnya
    bookmark = bookmarkData;
    // bookmarkData[0] adalah chapterIndex

    // nah dari sini ngecek apakah bookmark data ada di list bookmark
    var tempPIndex = bookmarkData[1]; // paragraph index
    var tempSIndex = bookmarkData[2]; // index tag <afd_booktag> dg id
    // afd_span
    // ngecek apakah bookmark yg di dapat ada di array bookmark
    // bookmark index adalah daftar bookmark yang telah di add sblumnya
    for ( var i = 0; i < bookmarkIndexArray.length; i++) {
        var pIndex = bookmarkIndexArray[i][0]; // paragraph index
        var sIndex = bookmarkIndexArray[i][1]; // span index
        if (pIndex == tempPIndex && sIndex == tempSIndex) {
            // $("#afd_bkImg").attr("src",
            // path + "/image/afd_bookmark_yellow.png");
            break;
        }
        var element;
        // kalau paragraph indexnya -1, element adalah seluruh kontenn
        if (pIndex == -1) {
            element = document.getElementById("afd_content");

        } else
            // kalau elemen 0 keatas, elemen adalah elemen pdalam afd konten
            // dengan nomor paragraph = pIndex
            element = $afd_content.find("p")[pIndex];

        // ini nyari element afdbooktag dalam paragraph elemen tadi ...
        var span = $(element).find("afdbooktag")[sIndex];

        // nah ini cara nyocokinnya, kita cari tau gimana yah ..
        // if (($afd_content.height() - 13) * (currentPage - 1) <
        // getElementTop(span)
        // && getElementTop(span) < ($afd_content.height() - 13)
        // * currentPage) {
        if (($afd_content.height()) * (currentPage - 1) < getElementTop(span)
            && getElementTop(span) < ($afd_content.height()) * currentPage) {
            // $("#afd_bkImg").attr("src",
            // path + "/image/afd_bookmark_yellow.png");
            break;
        }
    }

    // $afd_menu.show();
    // $afd_bottomMenu.show();

    var lastParagraphVisibleIndex = 0;
    var i = bookmarkData[1];
    if (i < $("p").length - 1)
        while ($("#afd_content p:eq(" + (i) + ")").is_on_screen()) {
            i++;
        }
    // console.log("last index : " + i + " --->>> " + $("p:eq("+i+")").text());

    Android.checkBookmarkData(bookmarkData[0], bookmarkData[1],
        bookmarkData[3], i);

// } else {
// $afd_menu.hide();
// // $afd_bottomMenu.hide();
// $afd_currentPage.show();
// // $afd_scale_panel.hide();
// // $tooltip.hide();
// }
// $afd_zoomin.hide();
// $afd_zoomout.hide();
}

/** replace the p text */
function replacePText() {
    Android.log("replacePText()");
    if (typeof (localStorage) == 'undefined') {
        alert('Your browser does not support HTML5 localStorage. Try upgrading.');
    } else {
        try {
            var tempBookmark = localStorage.getItem(bookIdentifier);
            Android
            .log("get bookmarks from local storage using bookIdentifier : "
                + bookIdentifier);
            Android.log("tempBookmark : " + tempBookmark);
            if (tempBookmark == null)
                return;

            var tempBookmarkArray = tempBookmark.split("afd_divide");
            var num = tempBookmarkArray.length;
            // Android.log("tempBookmarkArray : " + tempBookmarkArray);
            for ( var i = 0; i < num; i++) {
                var bookmarkData = tempBookmarkArray[i].split("afd_item");
                if (chapterIndex == bookmarkData[0]) {
                    var pIndex = bookmarkData[1];
                    var sIndex = bookmarkData[2];
                    var tempArray = new Array();
                    tempArray.push(pIndex);
                    tempArray.push(sIndex);
                    bookmarkIndexArray.push(tempArray);
                    var element;
                    if (pIndex == -1) {
                        element = document.getElementById("afd_content");
                    } else
                        element = $afd_content.find("p")[pIndex];
                    var pText = bookmarkData[4];
                    var svgElements = getSvgTag(element, 'svg');
                    var canvasElements = getSvgTag(element, 'canvas');
                    $(element).html(pText);
                    setSvgTag(element, svgElements, 'svg');
                    setSvgTag(element, canvasElements, 'canvas');
                }
            }
        } catch (e) {
            if (e == QUOTA_EXCEEDED_ERR) {
                alert('Quota exceeded!');
            }
        }
    }
}

// mendapatkan ukuran, ukuran chapter, ukuran buku, index chapter, jumlah
// chapter, identifier (Aspose), path extract folder epub (ke cache file), judul
// pages adalah jumlah halaman dari chapter, bukan keseluruhan buku
/**
 * Get the size from native code
 */
function getBookData(tempSubchapterId, tempSize, tempChapterSize, tempBookSize,
    tempChapterIndex, tempCurrentPage, tempchapterTotleNum, tempIdentifier,
    tempFilePath, title) {

    Android.log("getBookData(" + tempSubchapterId + "," + tempSize + ","
        + tempChapterSize + "," + tempBookSize + "," + tempChapterIndex
        + "," + tempchapterTotleNum + "," + tempIdentifier + ","
        + tempFilePath + "," + title + ")");
    subchapterId = tempSubchapterId;
    size = tempSize;
    chapterSize = tempChapterSize;
    bookSize = tempBookSize;
    chapterIndex = tempChapterIndex;
    chapterTotleNum = tempchapterTotleNum;
    bookIdentifier = tempIdentifier;
    path = tempFilePath;
    saveSettingData("afd_bookname", bookIdentifier);// Aspose
    $("#afd_title").html(title);
    setLayoutImag();
    replacePText();
    pages = getPages();

    if (subchapterId != '') {
        // currentPage = 1;
        // if(!$(subchapterId).visible(true)){
        // Android.log(subchapterId + " visible 1");
        // currentPage += 1;
        // leftPosition -= layoutWidth;
        // tempPosition = leftPosition;
        // // tempPosition -= layoutWidth;
        // // leftPositon = tempPosition;
        // $afd_content.animate({
        // left : leftPosition
        // }, 300, function(){
        // $afd_content.css("left",leftPosition + "px");
        // getReadingPercent();
        //				
        // });
        // }

        var element = $(subchapterId);
        Android.log("ELEMENT : " + element.scrollTop() + " : "
            + element.scrollLeft());
        Android.log("POSITION (TOP, LEFT) : (" + element.position().top + ","
            + element.position().left + ")");
        Android.log("OFFSET (TOP, LEFT) : (" + element.offset().top + ", "
            + element.offset().left + ")");
        currentPage = Math.floor(element.offset().left / layoutWidth);
        turahe = element.offset().left % +layoutWidth;
        if (turahe > 0)
            currentPage += 1;
        Android.log("CURRENT PAGE : " + currentPage);
    }

    else {
        currentPage = tempCurrentPage
    }
    if (currentPage >= pages) {
        currentPage = pages;
    }

    $afd_content.css("left", (0 - (currentPage - 1) * layoutWidth) + "px");
    tempPosition = leftPosition = $afd_content.css("left");
    Android.log("DARI GETBOOKDATA");
    getReadingPercent();
    percentTooltipPercent = currentPercent;

}

// menampilkan persentase baca
/** Figure out the percent */
function getReadingPercent() {
    Android.log("getReadingPercent()");
    // alert(pages+"::::"+currentPage);
    if (isNaN(currentPage))
        currentPage = 1;
    // value = (size (size apa ya?) + ukuran chapter * (halalman sedang dibaca /
    // jumlah halaman dari chapter)) / ukuran buku
    // size adalah ukuran komulatif dari chapter2 sebelumnya
    // jadi buku adalah jumlah kumulatif dari ukuran chapter sebelumnya +
    // (ukuran chapter sedang dibaca * (halaman chapter sedang dibaca / jumlah
    // halaman chapter sedang dibaca)) / ukuran buku
    // pengecualian untuk halaman pertama / cover tidak perlu dihitung

    currentPercent = (size + chapterSize * (currentPage / pages)) / bookSize;
    // sak jane iki ngopo toh .. diping satus, diping sepuluh ewu, njuk dibagi
    // sepuluh ewu :|
    // intine mung diping satus (didadeke persen)
    var value = parseInt(currentPercent * 100 * 1000000) / 1000000.0;
    $afd_currentPage.html(value + "%");
    // Android.setPercent(value);
    // alert("size : " + size + "\nchapterSize : " + chapterSize +
    // "\ncurrentPage : " + currentPage + "\npages : " + pages + "\nbookSize : "
    // + bookSize);
    getReadingPage();
}

// mengirimkan nilai index halaman chapter yg sedang dibaca dan jumlah halaman
// chapter yg sedang dibaca
/** Pass the reading data to native code */
function saveReadingData() {
    Android.log("saveReadingData()");
    Android.log("currentPage : " + currentPage);
    Android.log("pages : " + pages);
    Android.log("chapterIndex : " + chapterIndex);
    Android.log("currentPercent :" + currentPercent);
    Android.saveReadingData(currentPercent, currentPage);
    Android.currentReadingData(currentPage, pages);
}

/** Hidden the fontsize button */
/*
 * function hiddenFontSizeLayout() { Android.log("hiddenFontSizeLayout()");
 * $afd_zoomin.toggle(); $afd_zoomout.toggle(); }
 * 
 *//** Zoom in font size */
/*
 * function fontSizeZoomin() { Android.log("fontSizeZooming"); var fontSize =
 * $afd_content.css("font-size"); if (parseInt(fontSize) > 36) {
 * alert("Maximum"); return; } $afd_content.css("font-size", parseInt(fontSize) +
 * 3 + "px"); pages = getPages(); getReadingPercent();
 * saveSettingData("fontSize", parseInt(fontSize) + 3); saveReadingData(); }
 *//** Zoom out font size */
/*
 * function fontSizeZoomout() { Android.log("fontSizeZoomout"); var fontSize =
 * $afd_content.css("font-size"); if (parseInt(fontSize) < 14) {
 * alert("Minimum"); return; } $afd_content.css("font-size", parseInt(fontSize) -
 * 3 + "px"); pages = getPages(); if (currentPage > pages) { var i = currentPage -
 * pages; leftPosition = leftPosition + i * layoutWidth; tempPosition =
 * leftPosition; currentPage = pages; $afd_content.css({ "left" : leftPosition +
 * "px" }); } getReadingPercent(); saveSettingData("fontSize",
 * parseInt(fontSize) - 3); saveReadingData(); }
 */

// menentukan style lebar dan dan tinggi text judul buku
/** Resize the menu */
/*
 * function resizeMenu() { Android.log("resizeMenu()");
 * $("#afd_title").css("left", ($afd_menu.width() - $("#afd_title").width()) /
 * 2); $("#afd_title").css("top", ($afd_menu.height() -
 * $("#afd_title").height()) / 2); }
 */

// percent kok currentPage dikali 10000 dibagi jumlah halaman pages yo
function rotateScreen() {
    Android.log("rotateScreen()");
    var percent = currentPage * 10000 / pages;
    Android.log("PERCENT : " + percent);
    Android.log("CURRENT PAGE : " + currentPage);
    Android.log("PAGES : " + pages);
    pages = getPages();
    Android.log("PAGES AGAIN : " + pages);
    currentPage = parseInt((percent * pages) / 10000.0);
    Android.log("CURRENT PAGE AGAIN : " + currentPage);

    if ((currentPage - (percent * pages) / 10000.0) < 0) {
        currentPage = currentPage + 1;
    }

    leftPosition = 0;
    leftPosition = leftPosition - (currentPage - 1) * layoutWidth;
    tempPosition = leftPosition;
    $afd_content.css({
        "left" : leftPosition + "px"
    });
    Android.log("DARI ROTATE SCREEN");
    getReadingPercent();
}

// onStart .. saat on web di klik,
function onStart(ev) {

    // console.log("ON START ON START");
    Android.log("onStart(" + ev + ")");
    // menentukan showMenu == 1
    // menentukan koordinat x dan y ( startX dan startY)

    toggleMenu = 1;
    isMoving = 0;

    // pages = getPages();
    startX = ev.touches[0].pageX;
    startY = ev.touches[0].pageY;

    //	console.log("START (X,Y) : (" + startX + " / " + startY + ")");

    // jika chapterIndex = 0 ( chapter pertama ), currentPage = 1, koordinat x
    // kurang dari stengah layout maka tidak mencegah bergerak
    // jika chapterIndex = chapterTotal, currentPage == totalHalaman perbab, dan
    // koordinat x lebih dari stengah , maka tidak bergerak
    if ((chapterIndex == 0 && currentPage == 1 && startX < layoutWidth / 2)
        || (chapterIndex == chapterTotleNum && currentPage == getPages() && startX > layoutWidth / 2)) {
        preventMove = 1;
        return;
    }

    // alert(leftPosition+","+tempPosition);
    // cari posisi left dari konten
    leftPosition = $afd_content.position().left;
    Android.log("LEFT POSITION AGAIN : " + leftPosition);

    // ini apa ini
    if (tempPosition != leftPosition) {
        leftPosition = tempPosition;
    }

    // ini bug
    if (isNaN(leftPosition))
        leftPosition = (currentPage - 1) * -layoutWidth;
    // alert($(window).width() + " : " + currentPage + " : " + leftPosition);

    // defaultnya tidak dicegah bergerak
    preventMove = 0;
}

// saat on move ini diset lagi variable showMenu menjadi 0, menghide menu, dan
// menghide segala dialog
function onMove(ev) {
    // console.log("ON MOVE ON MOVE");
    // Android.log("onMove(" + ev + ")");

    toggleMenu = 0;

    android.selection.clearSelection();
	
    if (isMoving == 0) {
        Android.hideMenu();
        isMoving = 1;
    }

    // $afd_menu.hide();
    // $afd_bottomMenu.hide();
    $afd_scale_panel.hide();
    $afd_currentPage.show();
    // $afd_sharingBox.hide();
    $tooltip.hide();

    // kalau preventMode == 1, fngsi ini tidak dieksekusi
    if (preventMove == 1)
        return;

    ev.preventDefault();

    // koordinat X saat bergerak
    tempX = ev.touches[0].pageX;
    // move temp adalah jarak x terakhir dengan x awal
    moveTemp = tempX - startX;

    // ini mengupdate swipe, jadi sbenarnya saat move gitu, yg diupdate adalah
    // posisi leftnya ( dalam pixel )
    $("#afd_content").css({
        "left" : leftPosition + moveTemp + "px"
    });

}

// on end, ada apakah disini
// on end dieksekusi saat terakhir disentuh, apakah yang terjadi???
// jadi fungsi utamnya adalah mengeswipe, menentukan posisi left dan
// menganimasikan ke situ
// ini penanganan onstart, on move n on end, smua ditangani di disini
function onEnd(ev) {
    // console.log("ON END ON END");
    Android.log("onEnd(" + ev + ")");
    // kalau ada sharingbox, pas end di hide, menu juga dihilangin
    // if (!($afd_sharingBox.css('display') == 'none')) {
    // showMenu = 0;
    // $afd_sharingBox.hide()
    // }

    // kalau long touch , even ini tidak dieksekusi
    if (androidLongtouch == 1)
        return;

    // kalau show menu 1, start y > tinggi enu && start y kurang dari height
    // console.log("SHOW MENU : " + showMenu);
    // console.log("START Y : " + startY);
    // console.log("#afd_menu height : " + $('#afd_menu').height());
    // console.log("#afd_pageturn height : " + $('#afd_pageturn').height());
    // console.log("#afd_bottomMenu height : " + $('#afd_bottomMenu').height());

    // toggleMenu == 1
    if (toggleMenu == 1) {
        setBookmarkImg();
        Android.toggleMenu();
    }

    // kalau koordinat y yg disentuh berada
    // if (startY > $('#afd_menu').height() && startY <
    // $('#afd_pageturn').height() - $('#afd_bottomMenu').height()) {
    // setBookmarkImg();
    // Android.toggleMenu();
    // }

    // kalau prevent move, method ini tidak dieksekusi
    if (preventMove == 1)
        return;

    var halfWidth = layoutWidth / 2;

    // geser ke kanan
    // jika startnya stengah ke kanan
    if (startX >= halfWidth) {
        // ini apa ya..
        // misal halaman sekarang 3, jumlah halaman 5
        // move temp adalah jarak antara akhir sentuhan dengan start
        // jika currentPage < jumlah page && jarak < seperempat halaman
        if (currentPage < pages) {
            if (moveTemp < -halfWidth * 0.5) {
                tempPosition = leftPosition - layoutWidth;
                $("#afd_content").animate({
                    left : tempPosition
                }, 100);
                currentPage = currentPage + 1;
                getReadingPercent();

            } else {
                $("#afd_content").animate({
                    left : leftPosition
                }, 100);
            }
        } else if (isMoving == 1) {
            Android.log("Pindah chapter selanjutnya");
            // openChapter(chapterIndex, "next");
            resetPage();
            Android.nextChapter();
        }
    }

    // jika startnya setengah ke kiri
    else if (startX < halfWidth) {
        if (currentPage > 1) {
            if (moveTemp > halfWidth * 0.5) {
                tempPosition = leftPosition + layoutWidth;
                $("#afd_content").animate({
                    left : tempPosition
                }, 100);
                currentPage = currentPage - 1;
                getReadingPercent();

            } else {
                $("#afd_content").animate({
                    left : leftPosition
                }, 100);
            }
        } else if (isMoving == 1) {
            Android.log("Pindah chapter sebelumnya");
            // openChapter(chapterIndex, "preceding");
            resetPage();
            Android.prevChapter();
        }
    }

    Android.log("CURRENT PAGE AFTER END : " + currentPage);

    // var visible = $("#navPoint_38").visible(true, false);
    // Android.log("NAVPOINT_37 : "+ (visible ? "Ada":"Tidak"));

    moveTemp = 0;
    tempX = 0;
    saveReadingData();
// console.log("ON END");
// console.log("LEFT POSITION " + leftPosition);
// console.log("LAYOUT WIDTH " + layoutWidth);
// console.log("AFD_CONTENT OFFSET LEFT / TOP : " +
// $afd_content.offset().left + " / " + $afd_content.offset().top);
// console.log("AFD_CONTENT POSITION LEFT / TOP : " +
// $afd_content.position().left + " / " + $afd_content.position().top);
}

// menambahakan listener even pada tag - tag html
function addListener() {
    Android.log("addListener()");
    document.getElementById("afd_pageturn").addEventListener('touchend', onEnd,
        false);

    document.getElementById("afd_pageturn").addEventListener('touchmove',
        onMove, false);

    document.getElementById("afd_pageturn").addEventListener("touchstart",
        onStart, false);
/*
	 * document.getElementById("afd_zoom").addEventListener("click",
	 * hiddenFontSizeLayout, false);
	 * document.getElementById("afd_zoomin").addEventListener("click",
	 * fontSizeZoomin, false);
	 * document.getElementById("afd_zoomout").addEventListener("click",
	 * fontSizeZoomout, false);
	 * document.getElementById("afd_bookshelf").addEventListener("click", exit,
	 * false); document.getElementById("afd_TOC").addEventListener("click",
	 * function() { openPage("toc.html"); }, false);
	 * 
	 * document.getElementById("afd_bookmark").addEventListener("click",
	 * addBookmark, false);
	 */

// document.getElementById("afd_setting").addEventListener("click",
// function() {
// openPage("setting.html");
// }, false);
// document.getElementById("afd_jumping").addEventListener("click",
// function() {
// displayScalepanel("jumping")
// }, false);
// document.getElementById("afd_brightness").addEventListener("click",
// function() {
// displayScalepanel("brightness");
// }, false);
// document.getElementById("afd_precedingChapter").addEventListener("click",
// function() {
// openChapter(chapterIndex, "preceding");
// }, false);
// document.getElementById("afd_nextChapter").addEventListener("click",
// function() {
// openChapter(chapterIndex, "next");
// }, false);
}

// mendapatkan elemen dg tag svg atau canvas dr body
function getSvgTag(element, type) {
    Android.log("getSvgTag(" + element + "," + type + ")");
    var svgElements = new Array();
    var svgs;
    if (type == 'svg')
        svgs = element.getElementsByTagNameNS('http://www.w3.org/2000/svg',
            'svg');
    if (type == 'canvas')
        svgs = element.getElementsByTagName('canvas');
    if (svgs.length == 0)
        svgElements.push("0");
    for ( var i = 0; i < svgs.length; i++) {

        svgElements.push(svgs[i]);
    }
    return svgElements;
}

// mengganti tiap tag svg atau canvas dalam afd_content dengan svgElement
function setSvgTag(element, svgElements, type) {
    Android.log("setSvgTag(" + element + "," + svgElements + "," + type + ")");
    if (svgElements[0] == "0")
        return;
    var svgTemps;
    if (type == 'svg')
        svgTemps = element.getElementsByTagName('svg');
    if (type == 'canvas')
        svgTemps = element.getElementsByTagName('canvas');
    for ( var i = 0; i < svgTemps.length; i++) {
        var svgElement = svgElements[i];
        $(svgTemps[i]).replaceWith(svgElement);
    }
}

// menginisialisasi dan mendefinisikan tag-tag html, termasuk menu atas, bawah,
// pageturn, content, tooltip, scalePanel, sharing box
// twitter, google share serta menentukan lebar & tingginya
// juga menentukan lebar halaman, style, padding, serta menentukan variabel dari
// elemen
// menentukan ukuran menu
function initDom() {
    Android.log("initDom()");
    var svgElements = getSvgTag(document.body, 'svg');
    var canvasElements = getSvgTag(document.body, 'canvas');
    var bodyContent = document.body.innerHTML;

    $body = $("body");
    $body.empty();

    /*
	 * var menu = "<div id='afd_menu'>" + "<div id='afd_bookshelf'><img/></div>" + "<div
	 * id='afd_TOC'><img/></div>" + // "<div id='afd_title'></div>" + "<div
	 * id='afd_zoom'><img/></div>" + "<div id='afd_zoomout'><img/></div>" + "<div
	 * id='afd_zoomin'><img/></div>" + "<div id='afd_bookmark'><img
	 * id='afd_bkImg'/></div>" + "</div>";
	 */
    // var menu = "<div id='afd_menu'><div id='afd_TOC'><img/></div><div
    // id='afd_title'></div><div id='afd_zoom'><img/></div><div
    // id='afd_zoomout'><img/></div><div id='afd_zoomin'><img/></div><div
    // id='afd_bookmark'><img id='afd_bkImg'/></div></div>";
    /*
	 * var bottomMenu = "<div id='afd_bottomMenu'>" + "<div
	 * id='afd_precedingChapter'><img/></div>" + "<div id='afd_divide_1'
	 * class='afd_divide'><img/></div>" + "<div id='afd_jumping'><img/></div>" + "<div
	 * id='afd_divide_2' class='afd_divide'><img/></div>" + "<div
	 * id='afd_brightness'><img/></div>" + "<div id='afd_divide_3'
	 * class='afd_divide'><img/></div>" + "<div id='afd_setting'><img/></div>" + "<div
	 * id='afd_divide_4' class='afd_divide'><img/></div>" + "<div
	 * id='afd_nextChapter'><img/></div>" + "</div>";
	 */

    var pageturn = "<div id='afd_pageturn'></div>";
    var content = "<div id='afd_content'></div>";

    var tooltip = "<div id='tooltip'>" + "<div id='text'></div>"
    + "<img id='callout'/>" + "</div>";

    var scalePanel = "<div class='afd_scale_panel'>"
    + "<span id='afd_value'></span>"
    + "<div class='afd_scale' id='afd_bar'>" + "<div></div>"
    + "<span id='afd_btn'></span>" + "</div>" + "</div>";
    // var sharingBox = "<div id='afd_sharingBox'></div>";

    // $body.append(test);
    // $body.append(menu);
    // $body.append(sharingBox);

    /*
	 * var twitter = "<div class='afd_shareItem'>" + "<a
	 * id='afd_twitter_button' target='_blank' href ='#'>" + "<span><img/>Twitter</span>" + "</a>" + "</div>";
	 * 
	 * var googleShare = "<div class='afd_shareItem'>" + "<a id='afd_gplus'
	 * href='#'>" + "<span><img/>Google+</span>" + "</a>" + "</div>";
	 * 
	 * $afd_sharingBox = $("#afd_sharingBox");
	 * $afd_sharingBox.append(googleShare); $afd_sharingBox.append(twitter);
	 */

    $body.append(pageturn);

    $afd_pageturn = $("#afd_pageturn");
    $afd_pageturn.append(content);
    $afd_content = $("#afd_content");
    $body.append("<div id='afd_currentPage'></div>");
    $body.append(tooltip);
    $body.append(scalePanel);
    // $body.append(bottomMenu);

    $tooltip = $("#tooltip");
    $tooltip_text = $("#tooltip #text");
    $tooltip.hide();

    layoutHeight = $(window).height();
    layoutWidth = $(window).width();

    // $afd_menu = $("#afd_menu")
    // $afd_bottomMenu = $("#afd_bottomMenu");
    $afd_currentPage = $("#afd_currentPage");
    $afd_scale_panel = $(".afd_scale_panel");
    // $afd_zoomin = $("#afd_zoomin");
    // $afd_zoomout = $("#afd_zoomout");

    currentPageHeight = $afd_currentPage.height();

    if (layoutWidth >= 720) {
        padding = 40;
    // column gap bawaan css : 80px
    // $afd_content.css({
    // "padding-right" : "40px",
    // "padding-left" : "40px",
    // "padding-top" : "10px"
    // "padding" : "40px"
    // });

    // cWPadding = 80;
    // cHPadding = 35;
    // cHPadding = cWPadding + currentPageHeight;
    // di cssnya aslinya 15px, untuk yg lebar dibawah 720
    /*
		 * $("#afd_bookmark").css({ "right" : "40px" });
		 */

    // di cssnya aslinya 50px, untuk yg lebar dibawah 720
    /*
		 * $("#afd_zoom").css({ "right" : "75px" });
		 */
    // $("#afd_TOC").css({ "left":"110px"});
    // $("#afd_bookshelf").css({"left":"40px"});
    }

    if (layoutWidth < 720) {

        padding = 15;

    // gap sama dengan cWPadding
    // $afd_content.css({
    // "-webkit-column-gap" : "20px",
    // "padding" : "10px",
    // "padding-left" : "10px",
    // "padding-top" : "10px",
    // "padding":"10px"
    // });
    // cWPadding = 20;
    // cHPadding = 35;
    // cHPadding = cWPadding + currentPageHeight;
    }

    $afd_content.css("padding", padding + "px");
    cWPadding = padding * 2;
    cHPadding = cWPadding + currentPageHeight; // 10 tu bottomnya currentpage

    $afd_content.css("-webkit-column-gap", cWPadding + "px");

    // $afd_menu.width(layoutWidth);
    // $afd_bottomMenu.width(layoutWidth);
    $afd_scale_panel.width(layoutWidth);
    $afd_pageturn.width(layoutWidth);
    $afd_pageturn.height(layoutHeight);
    $afd_content.width(layoutWidth - cWPadding);
    $afd_content.height(layoutHeight - cHPadding);
    $afd_content.append(bodyContent);

    setSvgTag(document.getElementById("afd_content"), svgElements, 'svg');
    setSvgTag(document.getElementById("afd_content"), canvasElements, 'canvas');

    // $afd_content.append("<break id='afd_break'><br/>&#160;</break>");
    $("#afd_content img").css("maxWidth", (layoutWidth - 20) + "px");
    $("#afd_content audio").css("maxWidth", (layoutWidth - 20) + "px");
    $("#afd_content video").css("maxWidth", (layoutWidth - 20) + "px");

    if ($("#cover").attr("src") != "") {
        $("#cover").height($afd_content.height());
    }

    $("#tooltip").css("width", "500px");

    $("#tooltip img").css({
        "position" : "absolute",
        "top" : $("#tooltip #text").height() + 20 + $("#tooltip img").height()
    });

    // savedCurrentPage = readySettingData("currentPage");
    // if(savedCurrentPage == null)
    // currentPage = 1;

    // resizeMenu();

    // kalau diputer / ganti orientasi
    Android.log("DARI INIT DOM : WINDOW.RESIZE");
    $(window).resize(function() {

        layoutHeight = $(window).height();
        layoutWidth = $(window).width();
        // $afd_menu.width(layoutWidth);
        // $afd_bottomMenu.width(layoutWidth);
        $afd_scale_panel.width(layoutWidth);
        $afd_pageturn.width(layoutWidth);
        $afd_pageturn.height(layoutHeight);
        $afd_content.width(layoutWidth - cWPadding);
        $afd_content.height(layoutHeight - cHPadding);

        // $("#afd_content img").css("maxWidth", (layoutWidth - 20) + "px");
        // $("#afd_content audio").css("maxWidth",(layoutWidth - 20) + "px");
        // $("#afd_content video").css("maxWidth",(layoutWidth - 20) + "px");

        if ($("#cover").attr("src") != "") {
            $("#cover").height($afd_content.height());
        }

        // $("#tooltip").css("width", "500px");

        /*
		 * $("#tooltip img").css( { "position" : "absolute", "top" : $("#tooltip
		 * #text").height() + 20 + $("#tooltip img").height() });
		 */
        Android.log("WINDOWS NGERESIZE");
        rotateScreen();
    // resizeMenu();
    });
}

// mendapatkan data ebook dan meresize halaman, liat method
// BookView.resizePage()
/**
 * Invoke native code to pass data to js native code calls getBookData() and
 * resizePage()
 */
/**
 * Get the actual top
 */

// get element top itu dicari dengan offsetTop, current offsetPa
function getElementTop(element) {
    Android.log("getElementTop(" + element + ")");
    var actualTop = element.offsetTop;
    var current = element.offsetParent;
    // jika current tidak sama dengan null ...
    // dicari offsetTop aslinya, jadi offsetTop p ditambah offsetTop
    // containernya ..
    while (current !== null) {
        actualTop += current.offsetTop;
        current = current.offsetParent;
    }
    return actualTop;
}
function displayScalepanel(tag) {
    Android.log("displayScalepanel(" + tag + ")");
    $afd_scale_panel.toggle();
    // $afd_menu.hide();
    // $afd_bottomMenu.hide();
    $afd_currentPage.hide();

    if (tag == "jumping") {
        setInterval("getTitleFromPercent()", 100);
        window.clearInterval("getTitleFromPercent()", 300);
        $tooltip.show();
    }

    new scale('afd_btn', 'afd_bar', 'afd_value', tag);
}

scale = function(btn, bar, value, tag) {
    this.btn = document.getElementById(btn);
    this.bar = document.getElementById(bar);
    this.value = document.getElementById(value);
    this.step = this.bar.getElementsByTagName("div")[0];
    this.init(tag);
};

scale.prototype = {
    init : function(tag) {

        var afd_button = this.btn;
        var afd_bar = this.bar;
        var afd_step = this.step;
        var t = this;
        var floatPercent;
        var currentValue;
        var barWidth = $(afd_bar).width();

        if (tag == "jumping") {
            floatPercent = parseFloat($afd_currentPage.html());
            currentValue = floatPercent / 100 * barWidth;
        }
        if (tag == "brightness") {
            floatPercent = 1 - brightness;
            currentValue = floatPercent * barWidth;
        }

        afd_button.style.left = currentValue - 11 + "px";
        afd_step.style.width = currentValue + "px";

        layoutWidth = $(window).width();
        if (tag == "jumping") {
            currentValue = currentValue / barWidth;
            currentValue = parseInt(currentValue * 100 * 100) / 100.0;
            t.ondrag(currentValue + "%");
        }
        if (tag == "brightness") {
            currentValue = parseInt(floatPercent * 100) / 100.0;
            t.ondrag(currentValue);
        }

        if (tag == "jumping") {
            if ($(afd_button).offset().left > $("#tooltip").width() / 2
                + cWPadding
                && $(afd_button).offset().left + $("#tooltip").width() / 2
                + cWPadding < layoutWidth) {
                $("#tooltip").css(
                {
                    "left" : $(afd_button).offset().left
                    - $("#tooltip").width() / 2
                });

            } else if ($(afd_button).offset().left < layoutWidth / 2) {
                $("#tooltip").css({
                    "left" : cWPadding / 2
                });

            } else if ($(afd_button).offset().left > layoutWidth / 2) {
                $("#tooltip").css(
                {
                    "left" : layoutWidth - $("#tooltip").width()
                    - cWPadding / 2
                });

            }
        }

        $("#tooltip img").css({
            "left" : $(afd_button).offset().left - $("#tooltip").offset().left
        });

        afd_bar.ontouchstart = function(e) {
            var value = e.touches[0].pageX - $(afd_bar).offset().left;
            if (0 < value && value < barWidth) {
                afd_button.style.left = value - 11 + "px";
                afd_step.style.width = value + "px";
                value = value / barWidth;
                percentTooltipPercent = value;
                if (tag == "jumping") {
                    getTitleFromPercent();
                    value = parseInt(value * 100 * 100) / 100.0;
                    t.ondrag(value + "%");
                }
                if (tag == "brightness") {
                    value = parseInt(value * 100) / 100.0;
                    t.ondrag(value);
                    var tempBrightness = 1 - value;
                    t.onbrightness(tempBrightness);
                }
            }
        }

        afd_bar.ontouchmove = function(e) {
            var value = e.touches[0].pageX - $(afd_bar).offset().left;
            if (0 < value && value < barWidth) {
                afd_button.style.left = value - 11 + "px";
                afd_step.style.width = value + "px";
                value = value / barWidth;
                percentTooltipPercent = value;
                if (tag == "jumping") {
                    getTitleFromPercent();

                    if ($(afd_button).offset().left > ($("#tooltip").width() / 2)
                        + cWPadding
                        && $(afd_button).offset().left
                        + $("#tooltip").width() / 2 + cWPadding < layoutWidth) {
                        $("#tooltip").css(
                        {
                            "left" : $(afd_button).offset().left
                            - ($("#tooltip").width() / 2)
                        });

                    } else if ($(afd_button).offset().left < layoutWidth / 2) {
                        $("#tooltip").css({
                            "left" : cWPadding / 2
                        });

                    } else if ($(afd_button).offset().left > layoutWidth / 2) {
                        $("#tooltip").css(
                        {
                            "left" : layoutWidth
                            - $("#tooltip").width() - cWPadding
                            / 2
                        });

                    }

                    if ($(afd_button).offset().left >= $("#tooltip").offset().left
                        + $("#tooltip").width() - $("#tooltip img").width()) {
                        $("#tooltip img").css({
                            "left" : $("#tooltip").width() - 32
                        });

                    } else
                        $("#tooltip img").css(
                        {
                            "left" : $(afd_button).offset().left
                            - $("#tooltip").offset().left
                        });

                    value = parseInt(value * 100 * 100) / 100.0;
                    t.ondrag(value + "%");
                }
                if (tag == "brightness") {
                    value = parseInt(value * 100) / 100.0;
                    t.ondrag(value);
                    var tempBrightness = 1 - value;
                    t.onbrightness(tempBrightness);
                }
            }

            e.preventDefault();
        }
        afd_bar.ontouchend = function(e) {

            if (tag == "jumping") {
                var percent = $(afd_step).width() / barWidth;
                t.onjump(percent);
            }
            if (tag == "brightness") {
                saveSettingData("brightness", brightness);
            }
        }
    },
    ondrag : function(value) {
        this.value.innerHTML = value;
    },
    onjump : function(percent) {
        Android.sliderBarListener(percent);
    },
    onbrightness : function(tempBrightness) {
        brightness = tempBrightness;
        $afd_pageturn.css("background-color", "rgba(0,0,0," + tempBrightness
            + ")");
    }
}

// mengeset image untuk menu menu item, file image brada di folder image
function setLayoutImag() {
    Android.log("setLayoutImag()");
// $afd_menu.css("background-image", "url('" + path
// + "/image/afd_topmenu.png')");
// $("#afd_bookshelf img").attr("src", path + "/image/afd_back.png");
// $("#afd_TOC img").attr("src", path + "/image/afd_tablecontentsbtn.png");
// $("#afd_zoom img").attr("src", path + "/image/afd_fontsize.png");
// $("#afd_zoomout img").attr("src", path + "/image/afd_font_zoomout.png");
// $("#afd_zoomin img").attr("src", path + "/image/afd_font_zoomin.png");
// $("#afd_bookmark img").attr("src", path + "/image/afd_bookmark.png");
// $("#afd_precedingChapter img").attr("src", path + "/image/afd_prev.png");
// $("#afd_jumping img").attr("src", path + "/image/afd_skip.png");
// $("#afd_brightness img").attr("src", path + "/image/afd_bright.png");
// $("#afd_setting img").attr("src", path + "/image/afd_setting.png");
// $("#afd_nextChapter img").attr("src", path + "/image/afd_next.png");
// $(".afd_divide img").attr("src", path + "/image/afd_divide.png");
//
// $afd_bottomMenu.css("background-image", "url('" + path
// + "/image/afd_topmenu.png')");
// $(".afd_scale span").css("background-image",
// "url('" + path + "/image/afd_drug.png')");
//
// $("#tooltip img").attr("src", path + "/image/callout.png");
}

/**
 * Open the chapter
 * 
 * @param i
 *            is the chapter index
 */
// chapter 5, dari index 0 - 4
// buat buka chapter dari tombol prev, next, dan juga toc
function openChapter(i, order) {
    Android.log("openChapter(" + i + "," + order + ")");
    if (order == "preceding") {
        if (i == 0) {
            alert("first chapter");
            return;
        } else
            i = i - 1;
    } else if (order == "next") {
        if (i == chapterTotleNum - 1) {
            alert("last chapter");
            return;
        } else
            i = i + 1;
    }

    // Android.setCover(cover);
    currentPage = 1;
    Android.log("chapterNow " + i);
    Android.jsOpenChapter(i, order);
}

function exit() {
    Android.log("exit()");
    Android.exit();
}

// mendapatkan nilai dari settingan dg key tertentu
function readySettingData(key) {
    Android.log("readySettingData(" + key + ")");
    if (typeof (localStorage) == 'undefined') {
        alert('Your browser does not support HTML5 localStorage. Try upgrading.');
    } else {
        try {
            return localStorage.getItem(key);
        } catch (e) {
            if (e == QUOTA_EXCEEDED_ERR) {
                alert('Quota exceeded!');
            }
        }
    }
}

// menyimpan nilai settingan afd_pageturn dgn key tertentu dgan nilai tertentu
function saveSettingData(key, value) {
    Android.log("saveSettingData()");
    if (typeof (localStorage) == 'undefined') {
        alert('Your browser does not support HTML5 localStorage. Try upgrading.');
    } else {
        try {
            localStorage.setItem(key, value);
        } catch (e) {
            if (e == QUOTA_EXCEEDED_ERR) {
                alert('Quota exceeded!');
            }
        }
    }

}

// menginisialisasi dan mendefinisikan settingan halaman, seperti ukuran huruf,
// kecerahan, warna tulisan
// background, style huruf, modus malam dan siang dan mengesetnya ke
// afd_pageturn

function initSettings() {
    // cek settingan
    Android.log("initSettings()");
    // $afd_pageturn.find("*").css({"background-color":"rgba(0,0,0,0)"});

    var fontStyle = readySettingData("fontFamily"); // default = null/ times new
    // roman
    if (fontStyle != null || fontStyle == 'default') {
        $afd_pageturn.find("*").css({
            "font-family" : fontStyle
        });
    } else {
        $afd_pageturn.find("*").css({
            // "font-family" : "sans-serif"
            "font-family" : "'Times New Roman'"
        });
    }

    var fontSize = readySettingData("fontSize");
    if (fontSize != null) {
        $afd_content.css("font-size", parseInt(fontSize) + "px");
    } else {
        $afd_content.css("font-size", "14px");
    }

    var theme = readySettingData("theme");
    Android.log(theme);
    if (theme != null) {
        if (theme == "night") {
            $body.css({
                "background-color" : "black"
            });
            $afd_pageturn.find("*").css({
                "color" : "white"
            });
            $("#afd_currentPage").css("color", "white");

        } else if (theme == "sephia") {
            $body.css({
                "background-color" : "rgb(198, 192, 173)"
            });
            $afd_pageturn.find("*").css({
                "color" : "white"
            });

            $("#afd_currentPage").css("color", "white");
        } else { // day
            $body.css({
                "background-color" : "white"
            });
            $afd_pageturn.find("*").css({
                "color" : "black"
            });

            $("#afd_currentPage").css("color", "black");
        }
    } else { // day
        $body.css({
            "background-color" : "white"
        });
        $afd_pageturn.find("*").css({
            "color" : "black"
        });

        $("#afd_currentPage").css("color", "black");
    }

    var lineHeight = readySettingData("lineHeight");
    if (lineHeight != null) {
        $("#afd_content p").css({
            "line-height" : lineHeight
        });
    } else {
        $("#afd_content p").css({
            "line-height" : 1
        });
    }

    var brightness = readySettingData("brightness");
    // console.log("brightness " + brightness);
    if (brightness == null) {
        brightness = 0
    }

    // $afd_pageturn.css("opacity", brightness / 10);
    Android.setBrightness(parseInt(brightness));

    var fontColor = readySettingData("fontColor");
    if (fontColor != null) {
        $afd_pageturn.find("*").css({
            "color" : fontColor
        });
    }

    var background = readySettingData("background");
    if (background != null) {
        $body.css({
            "background-color" : background
        });
    }

}

function addNote(){
	var text = window.getSelection().toString();
        androidLongtouch = 0;
        showMenu = 0;
        Android.addNote(text);
}

function showSharingPage() {
    Android.log("showSharingPage");
    androidLongtouch = 0;
    showMenu = 0;
    var text = window.getSelection().toString();
    Android.shareText(text);
}

function androidLongtouchModel(model) {
    Android.log("androidLongtouchModel(" + model + ")");
    androidLongtouch = model;
    // alert("android long touch " + model);
    // saat terjadi longkclick, menu atas dan bawah dihide
    if (model == 1) {
        // $afd_menu.hide();
        // $afd_bottomMenu.hide();
        showMenu = 1;
        Android.showMenu();
    }
}

function androidCopySelectionText() {
    Android.log("androidCopySelectionText()");
    androidLongtouch = 0;
    var text = window.getSelection().toString();
    Android.copySelectionText(text);
}

function ttsSpeaking() {
    Android.log("ttsSpeaking()");
    androidLongtouch = 0;
    var text = window.getSelection().toString();
    Android.textToSpeak(text);
}

// mendapatkan versi dadri android
function getAndroidVersion(andVersion) {
    Android.log("getAndroidVersion(" + andVersion + ")");
    androidVersion = andVersion;
}

// mengeset view port menjadi ukuran device
function resetViewport() {
    Android.log("resetViewport()");
    var metas = $("head").find("meta");
    for ( var i = 0; i < metas.length; i++) {
        if ($(metas[i]).attr('name') == "viewport") {
            $(metas[i])
            .replaceWith(
                "<meta name=\"viewport\" content=\"width=device-width, height=device-height\"/>");
            return;
        }
    }
}

function getCover(bookCover) {
    Android.log("getCover()");
    cover = bookCover;
}

function setTitle(bookTitle) {
    Android.log("setTitle(" + bookTitle + ")");
    title = bookTitle;
}

function getTitleFromPercent() {
    Android.log("getTitleFromPercent()");
    Android.getTitleFromPercent(percentTooltipPercent);
    $tooltip_text.html(title);
}

function moveContentToCorrectPosition() {
    Android.log("moveContentToCorrentPosition()");
    leftPosition = (0 - ((currentPage - 1) * layoutWidth));
    Android.log("LEFT : " + leftPosition);
// $afd_content.css("left", left +"px");
// $afd_content.position().left = left + "px";
}

function setFontSetting(fontSize, fontFamily, lineHeight) {
    // console.log(fontSize + " : " + fontFamily + " : " + lineHeight);
    if (fontSize != -1)
        saveSettingData("fontSize", fontSize);
    if (fontFamily != null || fontFamily != -1)
        saveSettingData("fontFamily", fontFamily);
    if (lineHeight != -1)
        saveSettingData("lineHeight", lineHeight);
    initSettings();
    Android.resizePage();
}

function setThemeSetting(theme, brightness) {
    // console.log(theme + " : " + brightness);
    if (theme != null && theme != -1) {
        saveSettingData("theme", theme);

    }

    if (brightness != null && brightness != -1)
        saveSettingData("brightness", brightness);

    initSettings();
}

function getFontSetting() {
    var fontSize = readySettingData("fontSize");
    var fontFamily = readySettingData("fontFamily");
    var lineHeight = readySettingData("lineHeight");

    if (fontSize == null || fontSize == 0)
        fontSize = 14;
    if (fontFamily == null)
        fontFamily = "default";
    if (lineHeight == null || lineHeight == 0.0)
        lineHeight = 1;

    // console.log("fontSize " + fontSize + " fontFamily " + fontFamily + "
    // lineHeight " + lineHeight);

    Android.getFontSetting(parseInt(fontSize), fontFamily,
        parseFloat(lineHeight));
}

function getThemeSetting() {
    var theme = readySettingData("theme");
    var brightness = readySettingData("brightness");

    if (theme == null)
        theme = "day";
    if (brightness == null)
        brightness = 0;

    // console.log("theme : " + theme + " brightness : " + brightness);

    Android.getThemeSetting(theme, parseInt(brightness));

}

function resetPage() {
    currentPage = 1;
}

function scrollToParagraph(index) {
    var paragraph = $afd_content.find("p")[index];
    // var top = paragraph.offsetTop;//getElementTop(paragraph);
    //	console.log("offset pos "
    //			+ $("#afd_content p:eq(" + index + ")").offset().left + " / "
    //			+ $("#afd_content p:eq(" + index + ")").offset().top);
    //	console.log("offset pos "
    //			+ $("#afd_content p:eq(" + index + ")").position().left + " / "
    //			+ $("#afd_content p:eq(" + index + ")").position().top);
    currentPage = parseInt($("#afd_content p:eq(" + index + ")").offset().left
        / layoutWidth);
    leftPosition = -1 * currentPage * layoutWidth;
    //	console.log("currentPage " + currentPage + " , leftPosition "
    //			+ leftPosition);

    $("#afd_content").animate({
        left : leftPosition
    }, 0);
    getReadingPercent();
    Android.resetParagraphIndex();
}

function getReadingPage() {
    var pageNumber, percentPerPage;
    if (currentPage < pages) {
        var nextPercent = (size + chapterSize * ((currentPage + 1) / pages))
        / bookSize;
        percentPerPage = nextPercent - currentPercent;
    } else {
        var prevPercent = (size + chapterSize * ((currentPage - 1) / pages))
        / bookSize;
        percentPerPage = currentPercent - prevPercent;
    }

    //console.log("PER PAGE " + percentPerPage);
    pageNumber = currentPercent / percentPerPage;
    // console.log("PAGE NUMBER " + pageNumber);
    $afd_currentPage.text(currentPage);
}

$.fn.is_on_screen = function() {
    var win = $(window);
    var viewport = {
        top : win.scrollTop(),
        left : win.scrollLeft()
    };
    viewport.right = viewport.left + win.width();
    viewport.bottom = viewport.top + win.height();

    var bounds = this.offset();
    if (bounds == undefined)
        return false;
    bounds.right = bounds.left + this.outerWidth();
    bounds.bottom = bounds.top + this.outerHeight();

    return (!(viewport.right < bounds.left || viewport.left > bounds.right
        || viewport.bottom < bounds.top || viewport.top > bounds.bottom));
};

var found = 0;
jQuery.fn.highlight = function(pat) {
    //	console.log("fn highlighting --> " + pat);
    function innerHighlight(node, pat) {
        //		console.log("innerHighlight --> " + node);
        var skip = 0;
        if (node.nodeType == 3) {
            //			console.log("node type text");
            var pos = node.data.toUpperCase().indexOf(pat);
            //			console.log("pos --> " + pos);
            if (pos >= 0) {
                var spannode = document.createElement('span');
                spannode.className = 'highlight';
                var middlebit = node.splitText(pos);
                //				console.log("middlebit --> " + middlebit);
                var endbit = middlebit.splitText(pat.length);
                //				console.log("endbit --> " + endbit);
                var middleclone = middlebit.cloneNode(true);
                //				console.log("middleclone --> " + middleclone);
                spannode.appendChild(middleclone);
                //				console.log("spannode --> " + spannode);
                middlebit.parentNode.replaceChild(spannode, middlebit);
                skip = 1;
                found += 1;
            }
        } else if (node.nodeType == 1 && node.childNodes
            && !/(script|style)/i.test(node.tagName)) {
            //			console.log("node type element");
            //			console.log("html --> " + node.innerHTML);
            //			console.log("childnode length --> " + node.childNodes.length);
            for ( var i = 0; i < node.childNodes.length; ++i) {
                //console.log("i --> " + i);
                i += innerHighlight(node.childNodes[i], pat);
            }
        }
        return skip;
    }
    //	console.log("this length / pat length --> " + this.length + " / " + pat.length);
    return this.length && pat && pat.length ? this.each(function() {
        innerHighlight(this, pat.toUpperCase());
    }) : this;
};

jQuery.fn.removeHighlight = function() {
    return this.find("span.highlight").each(function() {
        this.parentNode.firstChild.nodeName;
        with (this.parentNode) {
            replaceChild(this.firstChild, this);
            normalize();
            }
    }).end();
};

function highlight(text) {
    //console.log('highlight');
    $("*").removeHighlight().highlight(text);
//	console.log("found " + text + " : " + found);
}

function highlightSelectedText() {
    highlighter.highlightSelection("highlight");
}

function noteSelectedText() {
    highlighter.highlightSelection("note");
}

function removeHighlightFromSelectedText() {
    highlighter.unhighlightSelection();
}
 
$(document).ready(function() {
    Android.log("document ready");

    rangy.init();

    highlighter = rangy.createHighlighter();

    highlighter.addClassApplier(rangy.createCssClassApplier("highlight", {
        ignoreWhiteSpace: true
    //,tagNames: ["span", "a"]
    }));

    highlighter.addClassApplier(rangy.createCssClassApplier("note", {
        ignoreWhiteSpace: true
    //        ,elementTagName: "a",
    //        elementProperties: {
    //            href: "#",
    //            onclick: function() {
    //                var highlight = highlighter.getHighlightForElement(this);
    //                if (window.confirm("Delete this note (ID " + highlight.id + ")?")) {
    //                    highlighter.removeHighlights( [highlight] );
    //                }
    //                return false;
    //            }
    //        }
    }));


    if (serializedHighlights) {
        highlighter.deserialize(serializedHighlights);
    } 
	
    resetViewport();
    initDom();
    initSettings();
    addListener();
    Android.resizeBgPage();

//	$("*").removeHighlight().highlight('katalog');
//	console.log("length : " + $("*").find("span.highlight").length);
//	console.log("left : " + $("*").find("span.highlight")[0].position().left);
//	console.log("top : " + $("*").find("span.highlight")[0].position().top);

});