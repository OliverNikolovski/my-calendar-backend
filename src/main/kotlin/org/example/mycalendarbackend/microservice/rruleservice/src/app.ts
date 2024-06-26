import express from 'express';
import bodyParser from 'body-parser';
import {datetime, Frequency, RRule} from "rrule";

const app = express();
const port = 3000;

app.use(bodyParser.json());

interface DateTime {
    year: number;
    month: number;
    day: number;
    hour: number | null;
    minute: number | null;
    second: number | null;
}

interface RRuleRequest {
    start: DateTime;
    end: DateTime | null;
    freq: string;
    count: number | null;
    byWeekDay: number[] | null;
    bySetPos: number | number[] | null;
    interval: number | null;
}

app.post('/generate-event-instances', (req, res) => {
    console.log('body:',req.body);
    const rruleRequests = req.body as RRuleRequest[];
    const rrules = rruleRequests.map(req => createRRuleFromRequest(req));
    const dates = rrules.map(rrule => rrule.all())
    res.send(dates);
});

// app.post('/generate-event-instances', (req, res) => {
//     console.log('body:',req.body);
//     const rruleRequests = req.body as RRuleRequest[];
//     const rrules = rruleRequests.map(req => createRRuleFromRequest(req));
//     const dates = rrules.flatMap(rrule => rrule.all())
//     res.send(dates);
// });

app.post('/generate-instances-for-events', (req, res) => {
    console.log('body:',req.body);
    const rrule = createRRuleFromRequest(req.body as RRuleRequest);
    res.send(rrule.all());
});

app.listen(port, () => {
    console.log(`App listening at http://localhost:${port}`);
});

function createRRuleFromRequest(request: RRuleRequest): RRule {
    return new RRule({
        dtstart: datetime(...dateTimeValues(request.start)),
        until: request.end ? datetime(...dateTimeValues(request.end)) : null,
        freq: Frequency[request.freq as keyof typeof Frequency],
        count: request.count ?? 20,
        byweekday: request.byWeekDay,
        bysetpos: request.bySetPos,
        interval: request.interval ?? 1
    });
}

function dateTimeValues(dateTime: DateTime): [number, number, number, number?, number?, number?] {
    return [dateTime.year, dateTime.month, dateTime.day,
        dateTime.hour ?? undefined, dateTime.minute ?? undefined, dateTime.second ?? undefined];
}