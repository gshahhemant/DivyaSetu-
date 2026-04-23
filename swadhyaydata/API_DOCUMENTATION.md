# Family Schedules API Documentation

## New REST API Endpoint

### GET `/api/vratin/families/schedules/{year}`

This endpoint returns all family names with their scheduled dates for a given year. The data is returned as a JSON object where each family name is a key and the value is an array of scheduled dates.

#### Purpose
- Get all families with their scheduled dates for a specific year
- Families without any schedules will still appear in the response with an empty array
- Uses LEFT JOIN to ensure all families are included regardless of whether they have schedules

#### Endpoint Details
- **URL**: `/api/vratin/families/schedules/{year}`
- **Method**: `GET`
- **URL Parameter**: `year` (integer) - The year to filter schedules (e.g., 2026)

#### Response Format
```json
{
  "Family Name 1": ["2026-01-15", "2026-02-20", "2026-03-10"],
  "Family Name 2": ["2026-01-22"],
  "Family Name 3": [],
  "Family Name 4": ["2026-04-05", "2026-05-18"]
}
```

#### Example Request
```bash
curl http://localhost:8080/api/vratin/families/schedules/2026
```

#### Example Response
```json
{
  "Patel Family": ["2026-01-15", "2026-03-20"],
  "Shah Family": ["2026-02-10"],
  "Desai Family": [],
  "Modi Family": ["2026-04-05", "2026-06-12", "2026-08-20"]
}
```

#### Implementation Details

The API executes the following SQL query:
```sql
SELECT vf.name, vs.schedule_date 
FROM vratin_family vf
LEFT JOIN vratin_schedule_mapping vm ON vm.family_srno = vf.sr_no
LEFT JOIN vratin_schedule vs ON vm.schedule_srno = vs.sr_no 
    AND EXTRACT(YEAR FROM vs.schedule_date) = :year
ORDER BY vf.name
```

The service layer processes the results and groups dates by family name:
- Uses a `LinkedHashMap` to maintain alphabetical order by family name
- Handles families with no schedules by including them with empty date lists
- Converts database date types to `LocalDate` for JSON serialization

#### Files Modified/Created

1. **VratinFamilyRepository.java** - Added custom query method
   - `findFamilySchedulesByYear(int year)` - Native SQL query with LEFT JOIN

2. **VratinSchedulerService.java** - Added service method
   - `getFamilySchedulesByYear(int year)` - Processes query results into Map

3. **VratinSchedulerController.java** - Added REST endpoint
   - `GET /api/vratin/families/schedules/{year}` - Returns family schedules map

#### Error Handling
- Returns HTTP 500 (Internal Server Error) if any exception occurs
- Returns HTTP 200 (OK) with the family schedules map on success

#### Notes
- The year filter is applied in the JOIN condition, not the WHERE clause
- This ensures families without schedules for that year are still included
- The response preserves alphabetical ordering by family name
- Empty arrays indicate families with no schedules for the specified year
