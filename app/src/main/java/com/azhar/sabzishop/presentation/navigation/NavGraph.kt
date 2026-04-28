package com.azhar.sabzishop.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.azhar.sabzishop.presentation.auth.*
import com.azhar.sabzishop.presentation.user.cart.CartScreen
import com.azhar.sabzishop.presentation.user.checkout.CheckoutScreen
import com.azhar.sabzishop.presentation.user.home.HomeScreen
import com.azhar.sabzishop.presentation.user.myorders.MyOrdersScreen
import com.azhar.sabzishop.presentation.user.ordersuccess.OrderSuccessScreen
import com.azhar.sabzishop.presentation.user.productdetails.ProductDetailsScreen
import com.azhar.sabzishop.presentation.user.profile.ProfileScreen
import com.azhar.sabzishop.presentation.admin.dashboard.AdminDashboardScreen
import com.azhar.sabzishop.presentation.admin.manageproducts.ManageProductsScreen
import com.azhar.sabzishop.presentation.admin.addproduct.AddProductScreen
import com.azhar.sabzishop.presentation.admin.editproduct.EditProductScreen
import com.azhar.sabzishop.presentation.admin.orderlist.OrderListScreen
import com.azhar.sabzishop.presentation.admin.orderdetails.OrderDetailsScreen
import com.azhar.sabzishop.presentation.admin.itemsales.ItemSalesScreen
import com.azhar.sabzishop.presentation.admin.feedback.AdminFeedbackScreen
import com.azhar.sabzishop.presentation.admin.revenue.RevenueScreen
import com.azhar.sabzishop.presentation.admin.stockplanning.StockPlanningScreen
import com.azhar.sabzishop.presentation.user.feedback.FeedbackScreen

/**
 * Root Navigation Graph.
 * Starts with Splash screen and branches into auth, user, or admin flows.
 */
@Composable
fun SabziNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        // ===================== SPLASH =====================
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToAuth = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToUser = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToAdmin = {
                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // ===================== AUTH FLOW =====================
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onSignUpClick = { navController.navigate(Screen.SignUp.route) },
                onGuestClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { role ->
                    val dest = if (role == "admin") Screen.AdminDashboard.route else Screen.Home.route
                    navController.navigate(dest) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onSignUpClick = { navController.navigate(Screen.SignUp.route) },
                onForgotPasswordClick = { navController.navigate(Screen.ForgotPassword.route) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(onBackClick = { navController.popBackStack() })
        }

        // ===================== USER FLOW =====================
        composable(Screen.Home.route) {
            HomeScreen(
                onProductClick = { productId ->
                    navController.navigate(Screen.ProductDetails.createRoute(productId))
                },
                onCartClick = { navController.navigate(Screen.Cart.route) },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onOrdersClick = { navController.navigate(Screen.MyOrders.route) },
                onFeedbackClick = { navController.navigate(Screen.Feedback.route) }
            )
        }

        composable(Screen.ProductDetails.route) {
            ProductDetailsScreen(
                onBackClick = { navController.popBackStack() },
                onCartClick = { navController.navigate(Screen.Cart.route) },
                onLoginRequired = { navController.navigate(Screen.Login.route) },
                onRelatedProductClick = { productId ->
                    navController.navigate(Screen.ProductDetails.createRoute(productId))
                }
            )
        }

        composable(Screen.Cart.route) {
            CartScreen(
                onBackClick = { navController.popBackStack() },
                onCheckoutClick = { navController.navigate(Screen.Checkout.route) },
                onLoginClick = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(Screen.Checkout.route) {
            CheckoutScreen(
                onBackClick = { navController.popBackStack() },
                onOrderPlaced = { orderId ->
                    navController.navigate(Screen.OrderSuccess.createRoute(orderId)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(Screen.OrderSuccess.route) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderSuccessScreen(
                orderId = orderId,
                onContinueShopping = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onViewOrders = { navController.navigate(Screen.MyOrders.route) }
            )
        }

        composable(Screen.MyOrders.route) {
            MyOrdersScreen(
                onBackClick = { navController.popBackStack() },
                onOrderClick = { orderId ->
                    navController.navigate(Screen.OrderDetails.createRoute(orderId, isAdmin = false))
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onBackClick = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onOrdersClick = { navController.navigate(Screen.MyOrders.route) }
            )
        }

        composable(Screen.Feedback.route) {
            FeedbackScreen(
                onBackClick = { navController.popBackStack() },
                onLoginRequired = { navController.navigate(Screen.Login.route) }
            )
        }

        // ===================== ADMIN FLOW =====================
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onManageProductsClick = { navController.navigate(Screen.ManageProducts.route) },
                onOrderListClick = { navController.navigate(Screen.OrderList.route) },
                onOrderClick = { orderId ->
                    navController.navigate(Screen.OrderDetails.createRoute(orderId, isAdmin = true))
                },
                onItemSalesClick = { navController.navigate(Screen.ItemSales.route) },
                onFeedbackClick = { navController.navigate(Screen.AdminFeedback.route) },
                onRevenueClick = { navController.navigate(Screen.AdminRevenue.route) },
                onStockPlanningClick = { navController.navigate(Screen.StockPlanning.route) },
                onLogout = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ManageProducts.route) {
            ManageProductsScreen(
                onBackClick = { navController.popBackStack() },
                onAddProductClick = { navController.navigate(Screen.AddProduct.route) },
                onEditProductClick = { productId ->
                    navController.navigate(Screen.EditProduct.createRoute(productId))
                }
            )
        }

        composable(Screen.AddProduct.route) {
            AddProductScreen(
                onBackClick = { navController.popBackStack() },
                onProductAdded = { navController.popBackStack() }
            )
        }

        composable(Screen.EditProduct.route) {
            EditProductScreen(
                onBackClick = { navController.popBackStack() },
                onProductUpdated = { navController.popBackStack() }
            )
        }

        composable(Screen.OrderList.route) {
            OrderListScreen(
                onBackClick = { navController.popBackStack() },
                onOrderClick = { orderId ->
                    navController.navigate(Screen.OrderDetails.createRoute(orderId, isAdmin = true))
                }
            )
        }

        composable(Screen.ItemSales.route) {
            ItemSalesScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Screen.AdminFeedback.route) {
            AdminFeedbackScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Screen.AdminRevenue.route) {
            RevenueScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Screen.StockPlanning.route) {
            StockPlanningScreen(onBackClick = { navController.popBackStack() })
        }

        composable(
            Screen.OrderDetails.route,
            arguments = listOf(
                androidx.navigation.navArgument("orderId") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("isAdmin") {
                    type = androidx.navigation.NavType.BoolType
                    defaultValue = false
                }
            )
        ) {
            OrderDetailsScreen(onBackClick = { navController.popBackStack() })
        }
    }
}

