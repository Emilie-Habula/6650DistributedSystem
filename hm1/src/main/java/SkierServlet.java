import javax.servlet.http.HttpServlet;

public class SkierServlet extends HttpServlet {
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("application/json");
    Gson gson = new Gson();

    try {
      StringBuilder sb = new StringBuilder();
      String s;
      while ((s = request.getReader().readLine()) != null) {
        sb.append(s);
      }

      Student student = (Student) gson.fromJson(sb.toString(), Student.class);

      Status status = new Status();
      if (student.getName().equalsIgnoreCase("edw")) {
        status.setSuccess(true);
        status.setDescription("success");
      } else {
        status.setSuccess(false);
        status.setDescription("not edw");
      }
      response.getOutputStream().print(gson.toJson(status));
      response.getOutputStream().flush();
    } catch (Exception ex) {
      ex.printStackTrace();
      Status status = new Status();
      status.setSuccess(false);
      status.setDescription(ex.getMessage());
      response.getOutputStream().print(gson.toJson(status));
      response.getOutputStream().flush();
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    processRequest(request, response);
  }
}
