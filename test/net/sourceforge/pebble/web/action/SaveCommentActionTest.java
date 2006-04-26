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

import net.sourceforge.pebble.domain.BlogEntry;
import net.sourceforge.pebble.domain.Comment;
import net.sourceforge.pebble.web.view.View;
import net.sourceforge.pebble.web.view.impl.CommentConfirmationView;

/**
 * Tests for the SaveCommentAction class.
 *
 * @author    Simon Brown
 */
public class SaveCommentActionTest extends SingleBlogActionTestCase {

  public void setUp() {
    action = new SaveCommentAction();

    super.setUp();
  }

  public void testProcess() throws Exception {
    BlogEntry blogEntry = blog.getBlogForToday().createBlogEntry();
    blog.getBlogForToday().addEntry(blogEntry);

    request.setParameter("entry", "" + blogEntry.getId());
    request.setParameter("parent", "");
    request.setParameter("title", "Test Title");
    request.setParameter("body", "Test Body");
    request.setParameter("author", "Test Author");
    request.setParameter("website", "http://www.somedomain.com");
    request.setParameter("submit", "Add Comment");

    request.setHeader("Referer", blog.getUrl() + "replyToBlogEntry.action");
    View view = action.process(request, response);
    assertTrue(view instanceof CommentConfirmationView);

    assertEquals(1, blogEntry.getComments().size());

    Comment comment = (Comment)blogEntry.getComments().get(0);
    assertEquals("Test Title", comment.getTitle());
    assertEquals("Test Body", comment.getBody());
    assertEquals("Test Author", comment.getAuthor());
    assertEquals("http://www.somedomain.com", comment.getWebsite());
  }

  public void testProcessWhenCommentsDisabled() throws Exception {
    BlogEntry blogEntry = blog.getBlogForToday().createBlogEntry();
    blog.getBlogForToday().addEntry(blogEntry);
    blogEntry.setCommentsEnabled(false);

    request.setParameter("entry", "" + blogEntry.getId());
    request.setParameter("parent", "");
    request.setParameter("title", "Test Title");
    request.setParameter("body", "Test Body");
    request.setParameter("author", "Test Author");
    request.setParameter("website", "Test Website");
    request.setParameter("submit", "Add Comment");

    View view = action.process(request, response);
    assertTrue(view instanceof CommentConfirmationView);
    assertEquals(0, blogEntry.getComments().size());
  }

}