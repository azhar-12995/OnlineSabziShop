package com.azhar.sabzishop.presentation.navigation

/**
 * Sealed class defining all navigable screens/routes in the app.
 */
sealed class Screen(val route: String) {
    // Auth
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object ForgotPassword : Screen("forgot_password")

    // User
    object Home : Screen("home")
    object ProductDetails : Screen("product_details/{productId}") {
        fun createRoute(productId: String) = "product_details/$productId"
    }
    object Cart : Screen("cart")
    object Checkout : Screen("checkout")
    object OrderSuccess : Screen("order_success/{orderId}") {
        fun createRoute(orderId: String) = "order_success/$orderId"
    }
    object MyOrders : Screen("my_orders")
    object Profile : Screen("profile")
    object Feedback : Screen("feedback")

    // Admin
    object AdminDashboard : Screen("admin_dashboard")
    object ManageProducts : Screen("manage_products")
    object AddProduct : Screen("add_product")
    object EditProduct : Screen("edit_product/{productId}") {
        fun createRoute(productId: String) = "edit_product/$productId"
    }
    object OrderList : Screen("order_list")
    object OrderDetails : Screen("order_details/{orderId}?isAdmin={isAdmin}") {
        fun createRoute(orderId: String, isAdmin: Boolean = false) = "order_details/$orderId?isAdmin=$isAdmin"
    }
    object ItemSales : Screen("item_sales")
    object AdminFeedback : Screen("admin_feedback")
    object AdminRevenue : Screen("admin_revenue")
    object StockPlanning : Screen("stock_planning")
}

