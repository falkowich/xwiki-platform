/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
require(["jquery", 'xwiki-meta'], function($, xm) {
  var update = function(element, url)
  {
    // TODO: show progress
    $.get({
      url : url,
      complete: function(xhr, textStatus) {
        switch (xhr.status) {
          case 200:
            // Add asynchronous meta tags
            var asyncHead = xhr.getResponseHeader('X-XWIKI-HTML-HEAD');
            if (asyncHead) {
              $('head').append(asyncHead);
            }

            // Replace the element by the asynchronous result
            element.replaceWith(xhr.responseText);

            // Insert the scripts after the HTML
            var asyncScripts = xhr.getResponseHeader('X-XWIKI-HTML-SCRIPTS');
            if (asyncScripts) {
              // FIXME: injecting <script> elements this way seems to generate a warning in some conditions
              $('head').append(asyncScripts);
            }

            break;

          case 202:
            // Display the spinner only if we wait more than one iteration
            showSpinner(element);

            update(element, url);
            break;

          default:
            // Get rid of the spinner since it's now useless
            element.remove();

            // TODO: error handling
        }
      }
    });
  }

  var showSpinner = function(element)
  {
    if (element.tagName == 'div') {
      element.html('<div class="fa fa-spinner fa-spin"/>');
    } else {
      element.html('<span class="fa fa-spinner fa-spin"/>');
    }
  }
  
  var activateAsyncPlaceHolder = function(element)
  {
    var url = element.dataset.xwikiAsyncUrl;

    // If the URL is not provided calculate it based on the id
    if (!url) {
        var id = element.dataset.xwikiAsyncId;

        if (id) {
          url = "${request.contextPath}/asyncrenderer/" + id;
        }
    }

    // Don't do anything if we don't have enough information
    if (!url) {
      return ;
    }

    var clientId = element.dataset.xwikiAsyncClientId;

    if (clientId) {
      url += '?clientId=' + clientId + '&';
    } else {
      url += '?';
    }

    url += 'timeout=' + 500 + '&wiki=' + xm.wiki;

    element = $(element);

    update(element, url);
  }

  var onMutations = function(mutations)
  {
    for (var i = 0; i < mutations.length; i++) {
      var mutation = mutations[i];

      for (var j = 0; j < mutation.addedNodes.length; j++) {
        var element = mutation.addedNodes[j];

        if (element.className == 'xwiki-async') {
          activateAsyncPlaceHolder(element);
        }
      }
    }
  }

  // Register a callback for when inserting an async placeholder in the DOM
  var observer = new MutationObserver(onMutations);
  observer.observe(document, { childList: true, subtree : true});

  // Activate all the place holders already in the DOM
  $(".xwiki-async").each(function(index, element) {
    activateAsyncPlaceHolder(element);
  });
});
