/*
 * Copyright (c) 2003-2006, Simon Brown
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 *
 *   - Neither the name of Pebble nor the names of its contributors may
 *     be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.sourceforge.pebble.web.action;

import net.sourceforge.pebble.Constants;
import net.sourceforge.pebble.comparator.BlogEntryComparator;
import net.sourceforge.pebble.domain.*;
import net.sourceforge.pebble.web.view.NotModifiedView;
import net.sourceforge.pebble.web.view.View;
import net.sourceforge.pebble.web.view.impl.AtomView;
import net.sourceforge.pebble.web.view.impl.RdfView;
import net.sourceforge.pebble.web.view.impl.RssView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Gets the RSS for a blog.
 *
 * @author    Simon Brown
 */
public class FeedAction extends Action {

  /**
   * Peforms the processing associated with this action.
   *
   * @param request  the HttpServletRequest instance
   * @param response the HttpServletResponse instance
   * @return the name of the next view
   */
  public View process(HttpServletRequest request, HttpServletResponse response) throws ServletException {
    AbstractBlog blog = (AbstractBlog)getModel().get(Constants.BLOG_KEY);
    String flavor = request.getParameter("flavor");

    SimpleDateFormat httpFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
    httpFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

    String ifModifiedSince = request.getHeader("If-Modified-Since");
    String ifNoneMatch = request.getHeader("If-None-Match");

    if (flavor != null && flavor.equalsIgnoreCase("atom")) {
      response.setContentType("application/atom+xml; charset=" + blog.getCharacterEncoding());
    } else {
      response.setContentType("application/xml; charset=" + blog.getCharacterEncoding());
    }

    Date lastModified = blog.getLastModified();
    response.setDateHeader("Last-Modified", lastModified.getTime());
    response.setHeader("ETag", "\"" + httpFormat.format(lastModified) + "\"");

    if (ifModifiedSince != null && ifModifiedSince.equals(httpFormat.format(lastModified))) {
      return new NotModifiedView();
    } else if (ifNoneMatch != null && ifNoneMatch.equals("\"" + httpFormat.format(lastModified) + "\"")) {
      return new NotModifiedView();
    } else {
      Tag tag = getTag(blog, request);
      Category category = getCategory(blog, request);
      String s = request.getParameter("includeAggregatedContent");
      boolean includeAggregatedContent = (s == null || s.equalsIgnoreCase("true"));

      List blogEntries;
      if (tag != null) {
        blogEntries = blog.getRecentBlogEntries(tag.getName());
        getModel().put("tag", tag);
      } else if (category != null) {
        blogEntries = blog.getRecentBlogEntries(category);
        getModel().put("category", category);
      } else {
        blogEntries = blog.getRecentBlogEntries();
      }
      List blogEntriesForFeed = new ArrayList();
      Iterator it = blogEntries.iterator();
      while (it.hasNext()) {
        BlogEntry entry = (BlogEntry)it.next();

        if (!includeAggregatedContent && entry.isAggregated()) {
          continue;
        } else {
          blogEntriesForFeed.add(entry);
        }
      }

      Collections.sort(blogEntriesForFeed, new BlogEntryComparator());

      getModel().put(Constants.BLOG_ENTRIES, blogEntriesForFeed);

      // set the locale of this feed request to be English
      javax.servlet.jsp.jstl.core.Config.set(
          request,
          javax.servlet.jsp.jstl.core.Config.FMT_LOCALE,
          Locale.ENGLISH);

      if (flavor != null && flavor.equalsIgnoreCase("atom")) {
        return new AtomView();
      } else if (flavor != null && flavor.equalsIgnoreCase("rdf")) {
        return new RdfView();
      } else {
        return new RssView();
      }
    }
  }

  /**
   * Helper method to find a named tag from a request parameter.
   *
   * @param abstractBlog      the blog for which the feed is for
   * @param request   the HTTP request containing the tag parameter
   * @return  a Tag instance, or null if the tag isn't
   *          specified or can't be found
   */
  private Tag getTag(AbstractBlog abstractBlog, HttpServletRequest request) {
    if (abstractBlog instanceof MultiBlog) {
      // getting tag based, aggregated feed isn't supported
      return null;
    } else {
      Blog blog = (Blog)abstractBlog;

      String tag = request.getParameter("tag");
      if (tag != null) {
        return blog.getTag(tag);
      } else {
        return null;
      }
    }
  }

  /**
   * Helper method to find a named category from a request parameter.
   *
   * @param abstractBlog      the blog for which the feed is for
   * @param request   the HTTP request containing the category parameter
   * @return  a Category instance, or null if the category isn't
   *          specified or can't be found
   */
  private Category getCategory(AbstractBlog abstractBlog, HttpServletRequest request) {
    if (abstractBlog instanceof MultiBlog) {
      // getting Category based, aggregated feed isn't supported
      return null;
    } else {
      Blog blog = (Blog)abstractBlog;

      String categoryId = request.getParameter("category");
      if (categoryId != null) {
        return blog.getCategory(categoryId);
      } else {
        return null;
      }
    }
  }

}