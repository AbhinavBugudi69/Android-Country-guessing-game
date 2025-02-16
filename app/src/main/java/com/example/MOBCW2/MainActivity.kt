package com.example.MOBCW2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import coil.compose.rememberImagePainter

class MainActivity : ComponentActivity() {
    private lateinit var db: footballdata

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            //defines the database
            db = footballdata.getDatabase(applicationContext)
            appNavigation()
        }
    }


    //this is the home screen with three buttons
    @Composable
    fun homeScreen(navController: NavHostController) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    navController.navigate("Add_Leagues_to_DB")
                },
                modifier = Modifier
                    .size(300.dp, 80.dp)
                    .padding(10.dp)
            ) {
                Text("Add Leagues To DB")
            }
            Button(
                onClick = { navController.navigate("Search_for_Clubs_By_League") },
                modifier = Modifier
                    .size(300.dp, 80.dp)
                    .padding(10.dp)
            ) {
                Text("Search For Clubs By League")
            }
            Button(
                onClick = { navController.navigate("Search_for_Clubs") },
                modifier = Modifier
                    .size(300.dp, 80.dp)
                    .padding(10.dp)
            ) {
                Text("Search For Clubs")
            }
        }

    }

    @Composable
    fun appNavigation() {
        //general navigation controls
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = "main_home") {
            composable("main_home") {
                homeScreen(navController)
            }
            composable("Add_Leagues_to_DB") {
                addleaguestodb(navController)
            }
            composable("Search_for_Clubs_By_League") {
                searchingclubsbyleague(navController)
            }
            composable("Search_for_Clubs") {
                searchingforclubs(navController)
            }
        }
    }



    @Composable
    fun addleaguestodb(navController: NavHostController) {
        //first button, getting info from a JSON string
        val jsonString = """
    {
        "leagues": [
            {"idLeague":"4328","strLeague":"English Premier League","strSport":"Soccer","strLeagueAlternate":"Premier League, EPL"},
            {"idLeague":"4329","strLeague":"English League Championship","strSport":"Soccer","strLeagueAlternate":"Championship"},
            {"idLeague":"4330","strLeague":"Scottish Premier League","strSport":"Soccer","strLeagueAlternate":"Scottish Premiership, SPFL"},
            {"idLeague":"4331","strLeague":"German Bundesliga","strSport":"Soccer","strLeagueAlternate":"Bundesliga, Fußball-Bundesliga"},
            {"idLeague":"4332","strLeague":"Italian Serie A","strSport":"Soccer","strLeagueAlternate":"Serie A"},
            {"idLeague":"4334","strLeague":"French Ligue 1","strSport":"Soccer","strLeagueAlternate":"Ligue 1 Conforama"},
            {"idLeague":"4335","strLeague":"Spanish La Liga","strSport":"Soccer","strLeagueAlternate":"LaLiga Santander, La Liga"},
            {"idLeague":"4336","strLeague":"Greek Superleague Greece","strSport":"Soccer","strLeagueAlternate":""},
            {"idLeague":"4337","strLeague":"Dutch Eredivisie","strSport":"Soccer","strLeagueAlternate":"Eredivisie"},
            {"idLeague":"4338","strLeague":"Belgian First Division A","strSport":"Soccer","strLeagueAlternate":"Jupiler Pro League"},
            {"idLeague":"4339","strLeague":"Turkish Super Lig","strSport":"Soccer","strLeagueAlternate":"Super Lig"},
            {"idLeague":"4340","strLeague":"Danish Superliga","strSport":"Soccer","strLeagueAlternate":""},
            {"idLeague":"4344","strLeague":"Portuguese Primeira Liga","strSport":"Soccer","strLeagueAlternate":"Liga NOS"},
            {"idLeague":"4346","strLeague":"American Major League Soccer","strSport":"Soccer","strLeagueAlternate":"MLS, Major League Soccer"},
            {"idLeague":"4347","strLeague":"Swedish Allsvenskan","strSport":"Soccer","strLeagueAlternate":"Fotbollsallsvenskan"},
            {"idLeague":"4350","strLeague":"Mexican Primera League","strSport":"Soccer","strLeagueAlternate":"Liga MX"},
            {"idLeague":"4351","strLeague":"Brazilian Serie A","strSport":"Soccer","strLeagueAlternate":""},
            {"idLeague":"4354","strLeague":"Ukrainian Premier League","strSport":"Soccer","strLeagueAlternate":""},
            {"idLeague":"4355","strLeague":"Russian Football Premier League","strSport":"Soccer","strLeagueAlternate":"Чемпионат России по футболу"},
            {"idLeague":"4356","strLeague":"Australian A-League","strSport":"Soccer","strLeagueAlternate":"A-League"},
            {"idLeague":"4358","strLeague":"Norwegian Eliteserien","strSport":"Soccer","strLeagueAlternate":"Eliteserien"},
            {"idLeague":"4359","strLeague":"Chinese Super League","strSport":"Soccer","strLeagueAlternate":""}
        ]
    }
    """

        var leagues by remember { mutableStateOf(listOf<LeagueInfo>()) }
        LaunchedEffect(Unit) {
            val leagueInfos = try {
                JSONObject(jsonString).getJSONArray("leagues").let { jsonArray ->
                    List(jsonArray.length()) { index ->
                        jsonArray.getJSONObject(index).let { leagueJson ->
                            LeagueInfo(
                                //creating variables and adding info
                                idLeague = leagueJson.getString("idLeague"),
                                strLeague = leagueJson.getString("strLeague"),
                                strSport = leagueJson.getString("strSport"),
                                strLeagueAlternate = leagueJson.getString("strLeagueAlternate")
                            )
                        }
                    }
                }
            } catch (e: JSONException) {
                Log.e("JSONError", "Error parsing leagues JSON: ${e.message}", e)
                listOf()
            }

            withContext(Dispatchers.IO) {
                //inserting info into the right database
                db.footballLeagueDao().insertleagues(leagueInfos)
                leagues = db.footballLeagueDao().getAllLeagues()
            }
        }

        Column(
            //general Back button
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        )  {
            Button(
                onClick = { navController.navigate("main_home") },
                modifier = Modifier.fillMaxWidth().padding(8.dp).background(Color.Black)
            ) {
                Text("Back to Home")
            }

            LazyColumn(modifier = Modifier.padding(8.dp).background(Color.Black)) {

                items(leagues) { league ->
                    //displaying items neatly and in a user friendly manner
                    LeagueItem(league)
                }
            }
        }
    }
    @Composable
    fun LeagueItem(league: LeagueInfo) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(width = 2.dp, color = Color.Gray, shape = RoundedCornerShape(8.dp))
                .padding(8.dp)
                .background(Color.Black)
        ) {
            Text(
                text = "League ID: ${league.idLeague}",color = Color.White
            )
            Text(
                text = "League Name: ${league.strLeague}",color = Color.White
            )
            Text(
                text = "Sport: ${league.strSport}",color = Color.White
            )
            league.strLeagueAlternate.takeIf { it.isNotEmpty() }?.let {
                Text(
                    text = "Alternate Names: $it",color = Color.White
                )
            }
        }
    }


    @Composable
    fun searchingclubsbyleague(navController: NavHostController) {

        //storing club name and the club info in separate variables

        var nameOfTheLeague by remember { mutableStateOf("") }
        var clubName by remember { mutableStateOf<List<clubInfo>>(emptyList()) }
        val coroutinescope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            TextField(
                value = nameOfTheLeague,
                onValueChange = { nameOfTheLeague = it },
                label = { Text("Enter League Name to search") },
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            )
            Button(
                onClick = {
                    coroutinescope.launch {
                        searchForClubs(nameOfTheLeague) { clubsgrabbed ->
                            clubName = clubsgrabbed
                        }
                    }
                },
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
            ) {
                //retrieves clubs by leagues
                Text("Retrieve Clubs", color = Color.White)
            }

            Button(
                onClick = {
                    if (clubName.isNotEmpty()) {
                        coroutinescope.launch {
                            withContext(Dispatchers.IO) {
                                db.ClubsDAO().insertclubs(clubName.map { club ->
                                    Club(
                                        id = club.idTeam,
                                        name = club.Name,
                                        shortName = club.strTeamShort,
                                        alternate = club.strAlternate,
                                        formedYear = club.intFormedYear,
                                        league = club.strLeague,
                                        stadium = club.strStadium,
                                        keywords = club.strKeywords,
                                        stadiumThumb = club.strStadiumThumb,
                                        stadiumLocation = club.strStadiumLocation,
                                        stadiumCapacity = club.intStadiumCapacity,
                                        website = club.strWebsite,
                                        teamJersey = club.strTeamJersey,
                                        teamLogo = club.strTeamLogo
                                    )
                                })
                            }
                        }
                    }
                },
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
            ) {
                //adding clubs to database and verifying
                Text("Save Clubs to Database", color = Color.White)
            }
            Button(
                onClick = {
                    //general back button
                    navController.popBackStack()
                },
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
            ) {
                Text("Back to Main Page", color = Color.White)
            }
            LazyColumn {
                items(clubName) { club ->
                    detailsOfClub(club)
                }
            }
        }
    }

    @Composable
    fun detailsOfClub(club: clubInfo) {
        Column(
            //displaying the details in a neat user friendly way
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(1.dp, Color.Gray, shape = RoundedCornerShape(8.dp))
                .padding(16.dp)
                .background(Color(0xFF121212))
        ) {
            Text(text = "Name: ${club.Name}", fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = "Short Name: ${club.strTeamShort}", color = Color.White)
            Text(text = "Alternate Names: ${club.strAlternate}", color = Color.White)
            Text(text = "Formed Year: ${club.intFormedYear}", color = Color.White)
            Text(text = "League: ${club.strLeague}", color = Color.White)
            Text(text = "League ID: ${club.idLeague}", color = Color.White)
            Text(text = "Stadium: ${club.strStadium}", color = Color.White)
            Text(text = "Keywords: ${club.strKeywords}", color = Color.White)
            Text(text = "Stadium Location: ${club.strStadiumLocation}", color = Color.White)
            Text(text = "Stadium Capacity: ${club.intStadiumCapacity}", color = Color.White)
            Text(text = "Website: ${club.strWebsite}", color = Color.White)
            Text(text = "Team Jersey: ${club.strTeamJersey}", color = Color.White)
            Text(text = "Team Logo: ${club.strTeamLogo}", color = Color.White)
        }
    }

    suspend fun searchForClubs(leaguename: String, onComplete: (List<clubInfo>) -> Unit) {
        try {
            //search for clubs by the league through the web api

            val encodedleague = URLEncoder.encode(leaguename, "UTF-8")
            val stringofurl = "https://www.thesportsdb.com/api/v1/json/3/search_all_teams.php?l=$encodedleague"
            Log.d("API", "URL: $stringofurl")

            val response = withContext(Dispatchers.IO) {
                val url = URL(stringofurl)
                val connection = url.openConnection() as HttpURLConnection
                val response = StringBuilder()
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                connection.disconnect()
                response.toString()
            }

            Log.d("API", "Response: $response")

            val clubs = parsingJson(response)
            onComplete(clubs)
        } catch (e: Exception) {
            Log.e("NetworkError", "Error fetching clubs: ${e.message}", e)
            onComplete(emptyList())
        }
    }

    fun parsingJson(json: String): List<clubInfo> {
        //converting json into variables
        val clubs = mutableListOf<clubInfo>()

        try {
            val jsonobject = JSONObject(json)
            val jsonarray = jsonobject.getJSONArray("teams")

            for (i in 0 until jsonarray.length()) {
                val clubJson = jsonarray.getJSONObject(i)

                val club = clubInfo(
                    idTeam = clubJson.getString("idTeam"),
                    Name = clubJson.getString("strTeam"),
                    strTeamShort = clubJson.getString("strTeamShort"),
                    strAlternate = clubJson.getString("strAlternate"),
                    intFormedYear = clubJson.getString("intFormedYear"),
                    strLeague = clubJson.getString("strLeague"),
                    idLeague = clubJson.getString("idLeague"),
                    strStadium = clubJson.getString("strStadium"),
                    strKeywords = clubJson.getString("strKeywords"),
                    strStadiumThumb = clubJson.getString("strStadiumThumb"),
                    strStadiumLocation = clubJson.getString("strStadiumLocation"),
                    intStadiumCapacity = clubJson.getString("intStadiumCapacity"),
                    strWebsite = clubJson.getString("strWebsite"),
                    strTeamJersey = clubJson.getString("strTeamJersey"),
                    strTeamLogo = clubJson.getString("strTeamLogo"),
                )
                clubs.add(club)
            }
        } catch (e: JSONException) {
            Log.e("JSONError", "Error parsing JSON: ${e.message}", e)
        }

        return clubs
    }


    @Composable
    fun searchingforclubs(navController: NavHostController) {

        //searching the clubs from database

        var textfieldsearch by remember { mutableStateOf("") }
        val clubs = remember { mutableStateOf<List<Club>>(emptyList()) }
        val coroutinescope = rememberCoroutineScope()

        // Main Column setup
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(16.dp)
        ) {
            TextField(
                value = textfieldsearch,
                onValueChange = { textfieldsearch = it },
                label = { Text("Enter Text of letters") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    coroutinescope.launch {
                        withContext(Dispatchers.IO) {
                            val searchingclubs = db.ClubsDAO().searchClubsWithQuery("%$textfieldsearch%")
                            clubs.value = searchingclubs
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Search", color = Color.White)
            }
            Button(
                onClick = {
                    navController.popBackStack()
                },
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
            ) {
                Text("Back to Main Page", color = Color.White)
            }

            LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                items(clubs.value) { club ->
                    ClubItem(club)
                }
            }
        }
    }

    @Composable
    fun ClubItem(club: Club) {
        //displaying the fetched items
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(2.dp, Color.White, RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Text(text = "Name: ${club.name}", fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = "Short Name: ${club.shortName}", color = Color.White)
            Text(text = "Alternate Name: ${club.alternate}", color = Color.White)
            Text(text = "Formed Year: ${club.formedYear}", color = Color.White)
            Text(text = "League: ${club.league}", color = Color.White)
            Text(text = "Stadium: ${club.stadium}", color = Color.White)
            Text(text = "Keywords: ${club.keywords}", color = Color.White)
            Text(text = "Stadium Location: ${club.stadiumLocation}", color = Color.White)
            Text(text = "Stadium Capacity: ${club.stadiumCapacity}", color = Color.White)
            Text(text = "Website: ${club.website}", color = Color.White)
            Text(text = "Team Jersey: ${club.teamJersey}", color = Color.White)

            club.teamLogo.takeIf { it.isNotEmpty() }?.let {
                Image(
                    painter = rememberImagePainter(it),
                    contentDescription = "Club Logo",
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.CenterHorizontally)
                )
            } ?: Text("No Logo Available", color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}




