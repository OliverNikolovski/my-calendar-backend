import express from 'express';
import bodyParser from 'body-parser';
import {datetime, Frequency, RRule} from "rrule";

const app = express();
const port = 3000;

app.use(bodyParser.json());

const defaultDateTime: DateTime = {
    year: (new Date()).getFullYear() + 2,
    month: 1,
    day: 1,
    hour: 0,
    minute: 0,
    second: 0
}

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
    const rruleRequests = req.body as RRuleRequest[];
    const rrules = rruleRequests.map(req => createRRuleFromRequest(req));
    const dates = rrules.map(rrule => rrule.all());
    res.send(dates);
});

app.post('/generate-event-instances-for-event', (req, res) => {
    const rruleRequest = req.body as RRuleRequest;
    const rrule = createRRuleFromRequest(rruleRequest);
    const dates = rrule.all();
    res.send(dates);
});

app.post('/get-rrule-text-and-string', (req, res) => {
    const rruleRequest = req.body as RRuleRequest;
    const rrule = createRRuleFromRequest(rruleRequest);
    res.send({
        rruleText: rrule.toText(),
        rruleString: rrule.toString()
    });
});

app.post('/calculate-previous-next-execution', (req, res) => {
    const rruleRequest = req.body.rruleRequest as RRuleRequest;
    const date = new Date(req.body.date as string);
    const rrule = createRRuleFromRequest(rruleRequest);
    const response = {
        previousOccurrence: rrule.before(date),
        nextOccurrence: rrule.after(date)
    }
    res.send(response);
});

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
        until: request.end ? datetime(...dateTimeValues(request.end)) : datetime(...dateTimeValues(defaultDateTime)),
        freq: Frequency[request.freq as keyof typeof Frequency],
        count: request.count,
        byweekday: request.byWeekDay,
        bysetpos: request.bySetPos,
        interval: request.interval ?? 1
    });
}

function dateTimeValues(dateTime: DateTime): [number, number, number, number?, number?, number?] {
    return [dateTime.year, dateTime.month, dateTime.day,
        dateTime.hour ?? undefined, dateTime.minute ?? undefined, dateTime.second ?? undefined];
}