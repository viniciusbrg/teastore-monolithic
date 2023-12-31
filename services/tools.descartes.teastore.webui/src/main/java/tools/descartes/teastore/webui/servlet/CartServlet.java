/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tools.descartes.teastore.webui.servlet;

import java.io.IOException;
import java.util.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import tools.descartes.teastore.auth.AuthFacade;
import tools.descartes.teastore.image.ImageFacade;
import tools.descartes.teastore.image.rest.ImageProviderEndpoint;
import tools.descartes.teastore.recommender.RecommenderFacade;
import tools.descartes.teastore.registryclient.Service;
import tools.descartes.teastore.registryclient.loadbalancers.LoadBalancerTimeoutException;

import tools.descartes.teastore.entities.Category;
import tools.descartes.teastore.entities.ImageSizePreset;
import tools.descartes.teastore.entities.OrderItem;
import tools.descartes.teastore.entities.Product;
import tools.descartes.teastore.entities.message.SessionBlob;
import tools.descartes.teastore.persistence.PersistenceFacade;

/**
 * Servlet implementation for the web view of "Cart".
 * 
 * @author Andre Bauer
 */
@WebServlet("/cart")
public class CartServlet extends AbstractUIServlet {
  private static final long serialVersionUID = 1L;

  /**
   * @see HttpServlet#HttpServlet()
   */
  public CartServlet() {
    super();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void handleGETRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, LoadBalancerTimeoutException {
    checkforCookie(request, response);
    SessionBlob blob = getSessionBlob(request);

    List<OrderItem> orderItems = blob.getOrderItems();
    ArrayList<Long> ids = new ArrayList<Long>();
    for (OrderItem orderItem : orderItems) {
      ids.add(orderItem.getProductId());
    }

    HashMap<Long, Product> products = new HashMap<Long, Product>();
    for (Long id : ids) {
      Product product = PersistenceFacade.getProduct(Service.PERSISTENCE, "products",
              Product.class, id);
      products.put(product.getId(), product);
    }

    request.setAttribute("storeIcon",
        ImageFacade.getWebImageIcon("icon"));
    request.setAttribute("title", "TeaStore Cart");
    request.setAttribute("CategoryList", PersistenceFacade.getAllCategories());
    request.setAttribute("OrderItems", orderItems);
    request.setAttribute("Products", products);
    request.setAttribute("login", AuthFacade.isLoggedIn(getSessionBlob(request)));

    List<Long> productIds = RecommenderFacade
        .getRecommendations(blob.getOrderItems(), blob.getUID());
    List<Product> ads = new LinkedList<Product>();
    for (Long productId : productIds) {
      ads.add(PersistenceFacade.getProduct(Service.PERSISTENCE, "products", Product.class,
          productId));
    }

    if (ads.size() > 3) {
      ads.subList(3, ads.size()).clear();
    }
    request.setAttribute("Advertisment", ads);

    request.setAttribute("productImages", ImageFacade.getProductPreviewImages(ads));

    request.getRequestDispatcher("WEB-INF/pages/cart.jsp").forward(request, response);

  }

}
