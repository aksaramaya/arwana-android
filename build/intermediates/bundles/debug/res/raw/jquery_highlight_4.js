/*

highlight v4

Highlights arbitrary terms.

<http://johannburkard.de/blog/programming/javascript/highlight-javascript-text-higlighting-jquery-plugin.html>

MIT license.

Johann Burkard
<http://johannburkard.de>
<mailto:jb@eaio.com>

*/

jQuery.fn.highlight = function(pat) {
console.log("fn highlight");
 function innerHighlight(node, pat) {
	 console.log("fn innerHighlight");
  var skip = 0;
  if (node.nodeType == 3) { // node type 3 itu text
	  console.log("node type 3 / text");
   var pos = node.data.toUpperCase().indexOf(pat);
   console.log("pos  " + pos);
   if (pos >= 0) {
	var spannode = document.createElement('span');
    spannode.className = 'highlight';
    var middlebit = node.splitText(pos);
    console.log("middlebit -> " + middlebit);
    var endbit = middlebit.splitText(pat.length);
    console.log("endbit -> " + endbit);
    var middleclone = middlebit.cloneNode(true);
    console.log("middleclone -> " + middleclone);
    spannode.appendChild(middleclone);
    middlebit.parentNode.replaceChild(spannode, middlebit);
    skip = 1;
   }
  }
  // note type 1 itu elemen
  else if (node.nodeType == 1 && node.childNodes && !/(script|style)/i.test(node.tagName)) {
	  console.log("node type 1 / element");
	  console.log("child node length " + node.childNodes.length);
   for (var i = 0; i < node.childNodes.length; ++i) {
	   console.log("i " + i);
    i += innerHighlight(node.childNodes[i], pat);
   }
  }
  return skip;
 }
 
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
